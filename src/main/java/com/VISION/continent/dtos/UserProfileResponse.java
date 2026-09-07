package com.VISION.continent.dtos;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserProfileResponse {

    @Schema(example = "1")
    private Long iduser;

    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String ville;

    @Schema(example = "15000.50")
    private BigDecimal soldeFcfa;

    private String statut;
}