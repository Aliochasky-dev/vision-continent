package com.VISION.continent.security;

import com.VISION.continent.service.VisionUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final VisionUserDetailsService visionUserDetailsService;

    @Value("${vision.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ─── Preflight CORS ──────────────────────────────────────
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ─── Swagger UI ──────────────────────────────────────────
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // ─── Auth publique (login, register, OTP, Google, reset) ─
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // ─── Racine / sante / page d'erreur interne ──────────────
                        .requestMatchers("/", "/index.html", "/error",
                                "/health", "/ping", "/actuator/health/**",
                                "/favicon.ico").permitAll()

                        // ─── Webhooks fournisseurs de paiement ───────────────────
                        .requestMatchers("/api/wallet/webhooks/**").permitAll()

                        // ─── ADMIN : ecritures sensibles (AVANT les regles GET) ──
                        .requestMatchers(HttpMethod.POST, "/api/categories", "/api/categories/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/categories/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/evenements")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/evenements/*")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/evenements/*/statut")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        .requestMatchers("/api/marches/*/resoudre")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers("/api/wallet/depot/*/confirmer")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers("/api/wallet/retrait/*/confirmer")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers("/api/transactions/depot/*/confirmer")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers("/api/transactions/retrait/*/confirmer")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        // ─── Lecture publique (sans connexion) ───────────────────
                        .requestMatchers(HttpMethod.GET,
                                "/api/evenements/**",
                                "/api/categories/**",
                                "/api/marches/**",
                                "/api/leaderboard",
                                "/api/activite"
                        ).permitAll()

                        // ─── Tout le reste : utilisateur connecte ────────────────
                        .anyRequest().authenticated()
                )
                // Sans ce bloc, Spring Security renvoyait un 403 au corps VIDE
                // aussi bien pour un token invalide que pour un role insuffisant.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** 401 : pas de token, token invalide/expire, compte suspendu... */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            Object raison = request.getAttribute(JwtAuthenticationFilter.ATTR_ERREUR_JWT);
            String message = raison != null
                    ? raison.toString()
                    : "Authentification requise : ajoutez un header 'Authorization: Bearer <token>'";
            ecrireJson(response, HttpServletResponse.SC_UNAUTHORIZED, message);
        };
    }

    /** 403 : authentifie, mais role insuffisant. */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                ecrireJson(response, HttpServletResponse.SC_FORBIDDEN,
                        "Acces refuse : role insuffisant pour " + request.getMethod()
                                + " " + request.getRequestURI());
    }

    private static void ecrireJson(HttpServletResponse response, int statut, String message)
            throws IOException {
        response.setStatus(statut);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"success\":false,\"message\":\"" + echapper(message) + "\",\"data\":null}");
    }

    private static String echapper(String valeur) {
        return valeur == null ? "" : valeur.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        // Plus flexible que setAllowedOrigins (gere mieux les variations)
        config.setAllowedOriginPatterns(origins.isEmpty()
                ? List.of("*")
                : origins);

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
