package com.VISION.continent.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class CategorieDto {

    @Getter @Setter
    @Schema(name = "CategorieRequest", description = "Requête de création/modification d'une catégorie")
    public static class Request {
        @NotBlank
        @Schema(example = "Sport")
        private String nom;

        @NotBlank
        @Schema(example = "sport")
        private String slug;

        @Schema(example = "Marchés sportifs camerounais")
        private String description;

        @Schema(example = "https://cdn.vision.cm/icons/sport.png")
        private String iconeUrl;
    }

    @Getter @Setter @Builder
    @Schema(name = "CategorieResponse")
    public static class Response {
        private UUID id;
        private String nom;
        private String slug;
        private String description;
        private String iconeUrl;
        private Boolean active;
        private long nbMarches;
    }
}