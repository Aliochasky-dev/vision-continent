package com.VISION.continent.dtos;

import com.VISION.continent.entity.Position;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** DTOs pour le classement (/api/leaderboard) et le flux d'activité (/api/activite). */
public class AnalyticsDto {

    @Getter @Setter @Builder
    @Schema(name = "LeaderboardRow", description = "Ligne du classement des traders")
    public static class LeaderboardRow {
        private int rang;
        private Long userId;
        private String nomComplet;
        private BigDecimal volumeFcfa;   // somme misée (critère de classement)
        private BigDecimal profitFcfa;   // total des gains encaissés
        private long nbTrades;
        private double tauxVictoire;     // 0..1
    }

    @Getter @Setter @Builder
    @Schema(name = "ActiviteRow", description = "Mise récente sur la plateforme")
    public static class ActiviteRow {
        private UUID positionId;
        private String nomComplet;
        private UUID evenementId;
        private String evenementTitre;
        private String marcheQuestion;
        private Position.Choix choix;
        private BigDecimal montantFcfa;
        private Position.Statut statut;
        private LocalDateTime createdAt;
    }
}
