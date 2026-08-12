package com.VISION.continent.controller;

import com.VISION.continent.dtos.GoogleOAuth2Dto;
import com.VISION.continent.service.GoogleAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
@Tag(name = "Authentification Google OAuth2", description = "Authentification via Google OAuth2")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    @GetMapping("/callback")
    @Operation(summary = "Callback OAuth2 Google",
            description = "Endpoint appelé après la redirection depuis Google après authentification. " +
                    "Extrait les données de l'utilisateur et retourne un JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JWT token retourné"),
            @ApiResponse(responseCode = "400", description = "Authentification Google échouée"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    public ResponseEntity<String> googleCallback(OAuth2AuthenticationToken authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body("Authentification Google échouée");
        }

        var attributes = authentication.getPrincipal().getAttributes();

        GoogleOAuth2Dto googleUser = GoogleOAuth2Dto.builder()
                .id((String) attributes.get("sub"))
                .email((String) attributes.get("email"))
                .nom((String) attributes.get("family_name"))
                .prenom((String) attributes.get("given_name"))
                .picture((String) attributes.get("picture"))
                .build();

        String token = googleAuthService.authenticateWithGoogle(googleUser);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    @Operation(summary = "Login Google pour mobile/SPA",
            description = "Authentifier un utilisateur via Google OAuth2. Utile pour les applications mobiles et SPA " +
                    "qui gèrent elles-mêmes le flux OAuth2.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JWT token retourné",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Données Google invalides"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de l'authentification")
    })
    public ResponseEntity<String> googleLogin(
            @RequestBody GoogleOAuth2Dto googleUser) {
        String token = googleAuthService.authenticateWithGoogle(googleUser);
        return ResponseEntity.ok(token);
    }
}
