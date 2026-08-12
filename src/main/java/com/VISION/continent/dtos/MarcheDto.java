package com.VISION.continent.dtos;

import com.VISION.continent.entity.Marche;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

public class MarcheDto {

    @Getter @Setter
    @Schema(name = "MarcheRequest", description = "Option à associer à un événement (candidat, seuil, équipe...)")
    public static class Request {
        @NotBlank
        @Schema(example = "Le Cameroun se qualifie-t-il ?")
        private String question;

        @Schema(example = "https://cdn.vision.cm/icons/cameroun.png")
        private String iconeUrl;

        @Schema(example = "Quintuple champion d'Afrique, le Cameroun aligne Onana et Anguissa...",
                description = "Infos sur cette option (palmarès, forme, biographie) pour guider le choix")
        private String description;
    }

    @Getter @Setter @Builder
    @Schema(name = "MarcheResponse")
    public static class Response {
        private UUID id;
        private String question;
        private String iconeUrl;
        private String description;
        private BigDecimal prixOui;
        private BigDecimal prixNon;
        private BigDecimal volumeFcfa;
        private Marche.Statut statut;
        private String outcomeGagnant;
    }

    @Getter @Setter
    @Schema(name = "MarcheResolveRequest", description = "Résolution d'un marché")
    public static class ResolveRequest {
        @NotBlank
        @Schema(example = "OUI", description = "OUI ou NON")
        private String outcomeGagnant;
    }
}