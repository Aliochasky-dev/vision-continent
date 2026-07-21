package com.VISION.continent.dtos;

import com.VISION.continent.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class AuthDto {

    @Getter
    @Setter
    public static class RegisterRequestDto {
        @NotBlank(message = "Le nom complet est obligatoire")
        private String nomComplet;

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Numéro de téléphone invalide")
        private String telephone;

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format email invalide")
        private String email;

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        private String password;

        @NotBlank(message = "La confirmation du mot de passe est obligatoire")
        private String confirmPassword;
    }

    @Getter @Setter
    public static class LoginRequest {
        @NotBlank(message = "Le téléphone ou l'email est obligatoire")
        private String identifiant;

        @NotBlank(message = "Le mot de passe est obligatoire")
        private String password;
    }

    @Getter @Setter
    public static class VerifyOtpRequest {
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format email invalide")
        private String email;

        @NotBlank(message = "Le code est obligatoire")
        @Size(min = 6, max = 6, message = "Le code doit contenir 6 chiffres")
        private String code;
    }

    @Getter @Setter
    public static class ResendOtpRequest {
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format email invalide")
        private String email;
    }

    @Getter @Setter @Builder
    public static class AuthResponse {
        private String token;
        private String telephone;
        private String nomComplet;
        private User.Role role;
        private BigDecimal soldeFcfa;
    }
}