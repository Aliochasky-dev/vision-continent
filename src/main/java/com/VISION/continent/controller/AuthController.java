package com.VISION.continent.controller;

import com.VISION.continent.dtos.ApiResponse;
import com.VISION.continent.dtos.AuthDto;
import com.VISION.continent.dtos.ForgotPasswordRequestDto;
import com.VISION.continent.dtos.ResetPasswordDto;
import com.VISION.continent.service.AuthService;
import com.VISION.continent.service.ResetPasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentification",
        description = """
                Création de compte, connexion et gestion des jetons.
                
                **Moyens de connexion** : téléphone + PIN / OTP, ou Google OAuth.
                
                **Jeton émis** — JWT à placer dans l'en-tête Authorization: Bearer <token>.
                
                Les routes de cette section sont publiques (aucun jeton requis), sauf /logout.
                """
)
public class AuthController {

    private final AuthService authService;
    private final ResetPasswordService resetPasswordService;

    @PostMapping("/register")
    @Operation(
            summary = "Créer un nouveau compte utilisateur",
            description = "Enregistrer un nouvel utilisateur avec ses informations personnelles. Un email de vérification OTP sera envoyé."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Compte créé avec succès",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Données invalides ou utilisateur déjà existant"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Erreur serveur"
            )
    })
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody AuthDto.RegisterRequestDto req) {
        String message = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(message, null));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Se connecter",
            description = "Authentifier un utilisateur avec téléphone + PIN. Retourne un JWT."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Connexion réussie, JWT retourné",
                    content = @Content(schema = @Schema(implementation = AuthDto.AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Identifiants invalides"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Email non vérifié — OTP requis"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Erreur serveur"
            )
    })
    public ResponseEntity<AuthDto.AuthResponse> login(
            @Valid @RequestBody AuthDto.LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Demander réinitialisation du mot de passe",
            description = "Envoyer un email avec un lien de réinitialisation."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Email de réinitialisation envoyé"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Email invalide ou utilisateur non trouvé"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Erreur lors de l'envoi de l'email"
            )
    })
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto req) {
        resetPasswordService.sendResetToken(req.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("Email de réinitialisation envoyé", null));
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Réinitialiser le mot de passe",
            description = "Réinitialiser le mot de passe avec le token reçu par email."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Mot de passe réinitialisé avec succès"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Token invalide ou expiré"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Erreur serveur"
            )
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordDto req) {
        resetPasswordService.resetPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("Mot de passe réinitialisé avec succès", null));
    }

    @PostMapping("/verify-email")
    @Operation(
            summary = "Vérifier l'email avec OTP",
            description = "Vérifier l'email avec le code OTP reçu. Retourne un JWT."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Email vérifié, JWT retourné",
                    content = @Content(schema = @Schema(implementation = AuthDto.AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "OTP invalide ou expiré"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Erreur serveur"
            )
    })
    public ResponseEntity<AuthDto.AuthResponse> verifyEmail(
            @Valid @RequestBody AuthDto.VerifyOtpRequest req) {
        return ResponseEntity.ok(authService.verifyEmail(req));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Se déconnecter",
            description = "Ajoute le token à la liste noire pour invalider la session."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Déconnexion réussie"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token invalide ou expiré"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Erreur serveur"
            )
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String message = authService.logout(token);
        return ResponseEntity.ok(ApiResponse.ok(message, null));
    }

    @PostMapping("/resend-otp")
    @Operation(
            summary = "Renvoyer le code OTP",
            description = "Envoyer à nouveau le code OTP à l'email de l'utilisateur."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "OTP renvoyé avec succès"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Email invalide"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Erreur lors de l'envoi du code"
            )
    })
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @Valid @RequestBody AuthDto.ResendOtpRequest req) {
        String message = authService.resendOtp(req);
        return ResponseEntity.ok(ApiResponse.ok(message, null));
    }
}