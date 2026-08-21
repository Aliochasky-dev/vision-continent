package com.VISION.continent.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secret);
    }

    public String generateToken(String email, String role) {
        return JWT.create()
                .withSubject(email)
                .withClaim("role", role)
                .withJWTId(UUID.randomUUID().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(getAlgorithm());
    }

    public String generateToken(UserDetails userDetails) {
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withClaim("role", role)
                .withJWTId(UUID.randomUUID().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(getAlgorithm());
    }

    public String extractEmail(String token) {
        return JWT.decode(token).getSubject();
    }

    public String extractRole(String token) {
        return JWT.decode(token).getClaim("role").asString();
    }

    public String extractJti(String token) {
        return JWT.decode(token).getId();
    }

    public Date extractExpiration(String token) {
        return JWT.decode(token).getExpiresAt();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            JWTVerifier verifier = JWT.require(getAlgorithm()).build();
            verifier.verify(token);

            String username = extractEmail(token);
            return username.equals(userDetails.getUsername());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        Date expiration = JWT.decode(token).getExpiresAt();
        return expiration.before(new Date());
    }
    // Nouvelle surcharge : génère le token avec l'ID utilisateur en claim
    public String generateToken(com.VISION.continent.entity.User user) {
        String role = "ROLE_" + user.getRole().name();
        return JWT.create()
                .withSubject(user.getTelephone())
                .withClaim("role", role)
                .withClaim("userId", user.getId()) // Long, pas besoin de toString()
                .withJWTId(java.util.UUID.randomUUID().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(getAlgorithm());
    }


    // Extrait l'ID utilisateur du token
    public Long extractUserId(String token) {
        return JWT.decode(token).getClaim("userId").asLong();
    }
}