package com.VISION.continent.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "Veuillez entrer votre email ou votre numéro de téléphone")
    private String identifier;
}