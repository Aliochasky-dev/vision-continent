package com.VISION.continent.dtos;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LoginResponse {

    @Schema(example = "5")
    private Long iduser;

    private String token;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;

    @Schema(example = "0.00")
    private BigDecimal soldeFcfa;

    @Schema(example = "Connexion réussie")
    private String message;
}
