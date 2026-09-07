package com.VISION.continent.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI visionOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("VISION Continent — API Documentation")
                        .version("1.0.0")
                        .description("""
                                API de la plateforme de prédiction **VISION Continent**.
                                
                                ### Comment tester une route protégée
                                1. Appeler une route d'authentification (`/api/auth/login` ou vérification OTP)
                                2. Copier le **token** (JWT) renvoyé dans la réponse
                                3. Cliquer sur **Authorize** en haut à droite de cette page
                                4. Coller le token (sans écrire le mot `Bearer`)
                                5. Valider puis tester les routes 🔒
                                
                                Les erreurs métier renvoient en général :
                                `{ "success": false, "message": "..." }`
                                
                                ### Modules
                                - **Authentification** : inscription, OTP, login, Google, reset password
                                - **Profil (Me)** : solde, stats
                                - **Événements & Marchés** : catalogue, détail
                                - **Positions** : placer une mise
                                - **Wallet / Transactions** : dépôts, retraits, historique
                                - **Notifications**
                                - **Analytics** : leaderboard, activité
                                """)
                        .contact(new Contact()
                                .name("Équipe VISION")
                                .email("nzimbaaliocha15@gmail.com"))
                        .license(new License().name("Propriétaire — VISION Continent")))
                .servers(List.of(

                        new Server()
                                .url("https://vision-continent.onrender.com")
                                .description("Production (Render)"),
                        new Server()
                                .url("http://localhost:8081")
                                .description("Local")

                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .description("JWT obtenu après login / vérification OTP. Coller uniquement le token.")));
    }
}