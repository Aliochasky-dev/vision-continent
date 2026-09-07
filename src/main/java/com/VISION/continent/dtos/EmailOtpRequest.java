package com.VISION.continent.dtos;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Requête pour envoyer OTP par email")
public class EmailOtpRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    @Schema(example = "nzimba@gmail.com")
    private String email;
}