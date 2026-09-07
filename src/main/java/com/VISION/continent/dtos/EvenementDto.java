package com.VISION.continent.dtos;

import com.VISION.continent.entity.Evenement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class EvenementDto {

    @Getter @Setter
    @Schema(name = "EvenementRequest", description = "Requête de création d'un événement")
    public static class Request {
        @NotBlank
        private String titre;

        @NotBlank
        private String contexte;

        @NotBlank
        private String reglesResolution;

        @NotBlank
        private String sourceResolution;

        @Schema(example = "https://cdn.vision.cm/icons/foot.png")
        private String iconeUrl;

        @NotNull
        private UUID categorieId;

        @NotNull
        @Schema(example = "UNIQUE", description = "UNIQUE (2 issues Oui/Non) ou MULTIPLE (plusieurs options)")
        private Evenement.TypeChoix typeChoix;

        @NotNull
        private LocalDateTime dateFin;

        @Schema(description = "Liste des options (marchés) associées")
        private List<MarcheDto.Request> marches;
    }

    @Getter @Setter @Builder
    @Schema(name = "EvenementResponse")
    public static class Response {
        private UUID id;
        private String titre;
        private String contexte;
        private String reglesResolution;
        private String sourceResolution;
        private String iconeUrl;
        private CategorieDto.Response categorie;
        private Evenement.TypeChoix typeChoix;
        private Evenement.Statut statut;
        private LocalDateTime dateFin;
        private LocalDateTime dateResolution;
        private BigDecimal volumeTotalFcfa;
        private Integer nbTraders;
        private LocalDateTime createdAt;
        private List<MarcheDto.Response> marches;
    }

    @Getter @Setter @Builder
    @Schema(name = "EvenementPageResponse")
    public static class PageResponse {
        private UUID id;
        private String titre;
        private String iconeUrl;
        private String categorieNom;
        private String categorieSlug;
        private Evenement.TypeChoix typeChoix;
        private BigDecimal volumeTotalFcfa;
        private Integer nbTraders;
        private LocalDateTime dateFin;
        private Evenement.Statut statut;
        private List<MarcheDto.Response> marches;
    }
}