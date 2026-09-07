package com.VISION.continent.service;

import com.VISION.continent.entity.User;
import com.VISION.continent.security.UserPrincipal;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * Generation / lecture des JWT.
 *
 * Convention unique (a ne plus casser) :
 *   sub    = telephone de l'utilisateur  -> c'est aussi UserDetails#getUsername()
 *   email  = email de l'utilisateur      (peut etre null pour de vieux tokens)
 *   userId = id numerique de l'utilisateur
 *   role   = role TOUJOURS prefixe "ROLE_" (ex: ROLE_ADMIN)
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secret);
    }

    private static String normaliserRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }

    // ─────────────────────────────────────────────────────────────
    //  GENERATION
    // ─────────────────────────────────────────────────────────────

    /** Methode de reference : tous les claims attendus par le filtre sont presents. */
    public String generateToken(User user) {
        return JWT.create()
                .withSubject(user.getTelephone())
                .withClaim("role", normaliserRole(user.getRole() != null ? user.getRole().name() : "USER"))
                .withClaim("userId", user.getId())
                .withClaim("email", user.getEmail())
                .withJWTId(UUID.randomUUID().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(getAlgorithm());
    }

    public String generateToken(UserDetails userDetails) {
        if (userDetails instanceof UserPrincipal principal) {
            return JWT.create()
                    .withSubject(principal.getUsername())
                    .withClaim("role", normaliserRole(principal.getRole()))
                    .withClaim("userId", principal.getId())
                    .withClaim("email", principal.getEmail())
                    .withJWTId(UUID.randomUUID().toString())
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                    .sign(getAlgorithm());
        }
        String role = userDetails.getAuthorities().isEmpty()
                ? "ROLE_USER"
                : userDetails.getAuthorities().iterator().next().getAuthority();
        return generateToken(userDetails.getUsername(), role);
    }

    public String generateToken(UserDetails userDetails, Long userId) {
        String role = userDetails.getAuthorities().isEmpty()
                ? "ROLE_USER"
                : userDetails.getAuthorities().iterator().next().getAuthority();
        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withClaim("role", normaliserRole(role))
                .withClaim("userId", userId)
                .withJWTId(UUID.randomUUID().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(getAlgorithm());
    }

    /** @param identifiant telephone (de preference) ou email de l'utilisateur. */
    public String generateToken(String identifiant, String role) {
        return JWT.create()
                .withSubject(identifiant)
                .withClaim("role", normaliserRole(role))
                .withJWTId(UUID.randomUUID().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(getAlgorithm());
    }

    // ─────────────────────────────────────────────────────────────
    //  LECTURE
    // ─────────────────────────────────────────────────────────────

    /**
     * Verifie la signature ET l'expiration, puis renvoie le token decode.
     * @throws JWTVerificationException si le token est invalide, altere ou expire.
     */
    public DecodedJWT verifier(String token) {
        JWTVerifier verifier = JWT.require(getAlgorithm()).build();
        return verifier.verify(token);
    }

    /**
     * Identifiant porte par le token : le "sub" en priorite, puis le claim "email"
     * pour rester compatible avec d'anciens tokens.
     */
    public String extractUsername(String token) {
        DecodedJWT jwt = JWT.decode(token);
        String subject = jwt.getSubject();
        if (subject != null && !subject.isBlank()) {
            return subject;
        }
        return jwt.getClaim("email").asString();
    }

    /** Email porte par le token (null si le claim est absent). */
    public String extractEmail(String token) {
        DecodedJWT jwt = JWT.decode(token);
        String email = jwt.getClaim("email").asString();
        if (email != null && !email.isBlank()) {
            return email;
        }
        // Vieux tokens : l'email etait parfois le subject.
        String subject = jwt.getSubject();
        return (subject != null && subject.contains("@")) ? subject : null;
    }

    public String extractRole(String token) {
        return normaliserRole(JWT.decode(token).getClaim("role").asString());
    }

    public String extractJti(String token) {
        return JWT.decode(token).getId();
    }

    public Date extractExpiration(String token) {
        return JWT.decode(token).getExpiresAt();
    }

    public Long extractUserId(String token) {
        return JWT.decode(token).getClaim("userId").asLong();
    }

    public boolean isTokenExpired(String token) {
        Date expiration = JWT.decode(token).getExpiresAt();
        return expiration == null || expiration.before(new Date());
    }

    /**
     * Valide signature + expiration + correspondance avec l'utilisateur charge en base.
     *
     * La correspondance accepte le telephone OU l'email : le "sub" du token est le
     * telephone, mais certains tokens historiques portent l'email.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }
        try {
            DecodedJWT jwt = verifier(token);   // leve si signature KO ou token expire

            String subject = jwt.getSubject();
            String email = jwt.getClaim("email").asString();

            if (subject != null && subject.equals(userDetails.getUsername())) {
                return true;
            }
            if (userDetails instanceof UserPrincipal principal) {
                if (subject != null && subject.equals(principal.getEmail())) {
                    return true;
                }
                if (email != null && (email.equals(principal.getEmail())
                        || email.equals(principal.getTelephone()))) {
                    return true;
                }
            }
            return email != null && email.equals(userDetails.getUsername());
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}
