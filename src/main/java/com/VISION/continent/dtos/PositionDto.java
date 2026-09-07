package com.VISION.continent.dtos;

import com.VISION.continent.entity.Position;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PositionDto {

    @Getter @Setter
    @Schema(name = "PositionRequest", description = "Placer une mise OUI ou NON sur un marché")
    public static class Request {
        @NotNull
        private UUID marcheId;

        @NotNull
        private Position.Choix choix;

        @NotNull
        @Positive
        @Min(value = 100, message = "Mise minimum : 100 FCFA")
        private BigDecimal montantFcfa;
    }

    @Getter @Setter @Builder
    @Schema(name = "PositionResponse")
    public static class Response {
        private UUID id;
        private UUID marcheId;
        private String marcheQuestion;
        private UUID evenementId;
        private String evenementTitre;
        private Position.Choix choix;
        private BigDecimal montantMiseFcfa;
        private Position.Statut statut;
        private LocalDateTime createdAt;
    }
}