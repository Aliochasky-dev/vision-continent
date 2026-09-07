package com.VISION.continent.security;

import com.VISION.continent.service.JwtUtil;
import com.VISION.continent.service.TokenBlacklistService;
import com.VISION.continent.service.VisionUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /** Cle d'attribut lue par le point d'entree pour expliquer le 401. */
    public static final String ATTR_ERREUR_JWT = "vision.jwt.error";
    /** Cle d'attribut lue par les controllers : @RequestAttribute("userId"). */
    public static final String ATTR_USER_ID = "userId";

    private final JwtUtil jwtUtil;
    private final VisionUserDetailsService visionUserDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Pas de token : on laisse passer, c'est la config Spring Security
        // qui decidera si la route est publique ou non.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            request.setAttribute(ATTR_ERREUR_JWT, "Token absent apres 'Bearer '");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                authentifier(request, token);
            }
        } catch (UsernameNotFoundException e) {
            request.setAttribute(ATTR_ERREUR_JWT, "Utilisateur du token introuvable");
            log.warn("JWT refuse : {}", e.getMessage());
        } catch (Exception e) {
            request.setAttribute(ATTR_ERREUR_JWT, "Token invalide ou expire");
            log.warn("JWT refuse : {}", e.toString());
        }

        filterChain.doFilter(request, response);
    }

    private void authentifier(HttpServletRequest request, String token) {

        // 1. Token revoque (logout) ? Redis indisponible => on ne bloque pas la requete.
        String jti = jwtUtil.extractJti(token);
        if (jti != null && tokenBlacklistService.estRevoque(jti)) {
            request.setAttribute(ATTR_ERREUR_JWT, "Session revoquee, reconnectez-vous");
            return;
        }

        // 2. Identifiant porte par le token (sub = telephone, sinon claim email).
        String identifiant = jwtUtil.extractUsername(token);
        if (identifiant == null || identifiant.isBlank()) {
            request.setAttribute(ATTR_ERREUR_JWT, "Token sans identifiant (sub)");
            return;
        }

        // 3. Chargement en base : c'est la BASE qui fait foi pour le role,
        //    jamais le claim "role" du token (sinon escalade de privileges).
        UserDetails userDetails = visionUserDetailsService.loadUserByUsername(identifiant);

        // 4. Signature + expiration + correspondance avec l'utilisateur charge.
        if (!jwtUtil.validateToken(token, userDetails)) {
            request.setAttribute(ATTR_ERREUR_JWT, "Token invalide ou expire");
            return;
        }

        if (!userDetails.isEnabled()) {
            request.setAttribute(ATTR_ERREUR_JWT, "Compte suspendu");
            return;
        }
        if (!userDetails.isAccountNonLocked()) {
            request.setAttribute(ATTR_ERREUR_JWT, "Compte banni");
            return;
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 5. userId expose aux controllers via @RequestAttribute("userId").
        //    Claim du token en priorite, sinon id du principal (vieux tokens sans userId).
        Long userId = jwtUtil.extractUserId(token);
        if (userId == null && userDetails instanceof UserPrincipal principal) {
            userId = principal.getId();
        }
        if (userId != null) {
            request.setAttribute(ATTR_USER_ID, userId);
        }
    }
}
