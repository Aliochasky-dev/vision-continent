package com.VISION.continent.security;

import com.VISION.continent.service.VisionUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

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
                        // ─── Swagger UI ──────────────────────────────────────────
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // ─── Auth publique ───────────────────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()

                        // ─── Racine (important pour Railway) ─────────────────────
                        .requestMatchers("/", "/index.html", "/health", "/ping").permitAll()

                        // ─── Webhooks publics (appelés par les fournisseurs de paiement) ─
                                .requestMatchers("/api/wallet/webhooks/**").permitAll()

                        // ─── Auth publique ───────────────────────────────────────
                                .requestMatchers("/api/auth/**").permitAll()

                        // ─── Lecture publique (sans connexion) ───────────────────
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/api/evenements/**",
                                "/api/categories/**",
                                "/api/marches/**",
                                "/api/leaderboard",
                                "/api/activite"
                        ).permitAll()

                        // ─── ADMIN uniquement ────────────────────────────────────
                        .requestMatchers("/api/marches/*/resoudre").hasRole("ADMIN")
                        .requestMatchers("/api/transactions/depot/*/confirmer").hasRole("ADMIN")
                        .requestMatchers("/api/transactions/retrait/*/confirmer").hasRole("ADMIN")
                        .requestMatchers("/api/evenements/*/statut").hasRole("ADMIN")
                        .requestMatchers("/api/categories/*/toggle").hasRole("ADMIN")

                        // ─── Tout le reste : utilisateur connecté ────────────────
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
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