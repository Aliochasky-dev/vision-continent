package com.VISION.continent.dtos;

import com.VISION.continent.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** DTOs pour l'utilisateur connecté : profil (/api/me) et statistiques (/api/me/stats). */
public class MeDto {

    @Getter @Setter @Builder
    @Schema(name = "MeResponse", description = "Profil de l'utilisateur connecté")
    public static class MeResponse {
        private Long id;
        private String nomComplet;
        private String telephone;
        private String email;
        private User.Role role;
        private BigDecimal soldeFcfa;
    }

    @Getter @Setter @Builder
    @Schema(name = "MeStatsResponse", description = "Statistiques agrégées de l'utilisateur")
    public static class StatsResponse {
        private BigDecimal soldeFcfa;
        private BigDecimal pnlTotalFcfa;     // gains encaissés - mises des positions résolues
        private BigDecimal volumeTradeFcfa;  // somme des mises
        private long nbPositions;
        private long nbActives;
        private long nbGagnees;
        private long nbPerdues;
        private double tauxVictoire;         // 0..1 sur les positions résolues
        private LocalDateTime membreDepuis;
    }
}
