package com.VISION.continent.dtos;

import lombok.Builder;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;



@Data
@Builder
public class AuthResponse {

    @Schema(example = "2")
    private Long iduser;

    @Schema(example = "eyJhbGciOiJIUzUxMiJ9...")
    private String token;

    @Schema(example = "Nzioba")
    private String nom;

    @Schema(example = "Jekn")
    private String prenom;

    @Schema(example = "nzimbo@gmail.com")
    private String email;

    @Schema(example = "+237697123456")
    private String telephone;

    @Schema(example = "Yaoundé")
    private String ville;

    @Schema(example = "0.00")
    private BigDecimal soldeFcfa;

    @Schema(example = "user")
    private String statut;

    @Schema(example = "Inscription réussie avec succès")
    private String message;
}