package com.VISION.continent.controller;

import com.VISION.continent.dtos.GoogleOAuth2Dto;
import com.VISION.continent.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    /**
     * Endpoint appelé après connexion Google (OAuth2 callback)
     */
    @GetMapping("/callback")
    public ResponseEntity<String> googleCallback(OAuth2AuthenticationToken authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body("Authentification Google échouée");
        }

        // Extraction des informations depuis Google
        var attributes = authentication.getPrincipal().getAttributes();

        GoogleOAuth2Dto googleUser = GoogleOAuth2Dto.builder()
                .id((String) attributes.get("sub"))
                .email((String) attributes.get("email"))
                .nom((String) attributes.get("family_name"))
                .prenom((String) attributes.get("given_name"))
                .picture((String) attributes.get("picture"))
                .build();

        // Créer ou authentifier l'utilisateur
        String token = googleAuthService.authenticateWithGoogle(googleUser);

        // Pour le moment on retourne le token en JSON
        // Tu pourras plus tard rediriger vers ton frontend
        return ResponseEntity.ok(token);
    }

    /**
     * Endpoint alternatif pour les applications mobiles ou frontend SPA
     */
    @PostMapping("/login")
    public ResponseEntity<String> googleLogin(@RequestBody GoogleOAuth2Dto googleUser) {
        String token = googleAuthService.authenticateWithGoogle(googleUser);
        return ResponseEntity.ok(token);
    }
}