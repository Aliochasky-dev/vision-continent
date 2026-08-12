package com.VISION.continent.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class CommentaireDto {

    @Getter @Setter
    @Schema(name = "CommentaireRequest", description = "Publier un commentaire")
    public static class Request {
        @NotBlank
        @Size(max = 1000)
        private String contenu;
    }

    @Getter @Setter @Builder
    @Schema(name = "CommentaireResponse")
    public static class Response {
        private UUID id;
        private Long auteurId;
        private String auteurNom;
        private String contenu;
        private Integer nbLikes;
        private Boolean likeParMoi;
        private LocalDateTime createdAt;
    }
}