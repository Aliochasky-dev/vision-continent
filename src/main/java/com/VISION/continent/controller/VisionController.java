package com.VISION.continent.controller;

import com.VISION.continent.dtos.*;
import com.VISION.continent.entity.*;
import com.VISION.continent.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Vision API", description = "Gestion des catégories, événements, positions, transactions, commentaires, profil et notifications")
public class VisionController {

    private final CategorieService categorieService;
    private final EvenementService evenementService;
    private final PositionService positionService;
    private final CommentaireService commentaireService;
    private final UserService userService;
    private final AnalyticsService analyticsService;
    private final NotificationService notificationService;

    // ═══════════════════════════════════════════════════════════════
    // CATÉGORIES
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/api/categories")
    @Operation(summary = "Lister toutes les catégories", description = "Récupère la liste complète des catégories disponibles")
    
    public ResponseEntity<ApiResponse<List<CategorieDto.Response>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.ok(categorieService.findAll()));
    }

    @GetMapping("/api/categories/{slug}")
    @Operation(summary = "Détail d'une catégorie", description = "Récupère les informations détaillées d'une catégorie par son slug")
    
    public ResponseEntity<ApiResponse<CategorieDto.Response>> getCategorieBySlug(
            @Parameter(description = "Slug unique de la catégorie", required = true, example = "sports")
            @PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(categorieService.findBySlug(slug)));
    }

    @PostMapping("/api/categories")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Créer une catégorie", description = "Crée une nouvelle catégorie (Admin seulement)")
    public ResponseEntity<ApiResponse<CategorieDto.Response>> createCategorie(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Données de la catégorie",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CategorieDto.Request.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody CategorieDto.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Catégorie créée", categorieService.create(req)));
    }

    @PutMapping("/api/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Modifier une catégorie", description = "Met à jour les informations d'une catégorie (Admin seulement)")
    
    public ResponseEntity<ApiResponse<CategorieDto.Response>> updateCategorie(
            @Parameter(description = "ID de la catégorie à modifier", required = true)
            @PathVariable UUID id,
            @RequestBody(
                    description = "Nouvelles données de la catégorie",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CategorieDto.Request.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody CategorieDto.Request req) {
        return ResponseEntity.ok(ApiResponse.ok(categorieService.update(id, req)));
    }

    @PatchMapping("/api/categories/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Basculer l'état actif/inactif d'une catégorie", description = "Active ou désactive une catégorie (Admin seulement)")
    
    public ResponseEntity<ApiResponse<Void>> toggleCategorie(
            @Parameter(description = "ID de la catégorie", required = true)
            @PathVariable UUID id) {
        categorieService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ═══════════════════════════════════════════════════════════════
    //  ÉVÉNEMENTS
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/api/evenements")
    @Operation(summary = "Catalogue des événements", description = "Récupère la liste paginée des événements disponibles, avec filtrage optionnel par catégorie et tri")

    public ResponseEntity<ApiResponse<Page<EvenementDto.PageResponse>>> getCatalogue(

            @Parameter(description = "Filtrer par catégorie (slug)", example = "sports")
            @RequestParam(required = false)          String categorie,
            @Parameter(description = "Tri des événements: recent, trending, closing_soon", example = "recent")
            @RequestParam(defaultValue = "recent")   String sort,
            @Parameter(description = "Numéro de la page (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0")        int    page,
            @Parameter(description = "Nombre d'événements par page", example = "9")
            @RequestParam(defaultValue = "9")        int    size) {
        return ResponseEntity.ok(ApiResponse.ok(
                evenementService.getCatalogue(categorie, sort, page, size)));
    }

    @GetMapping("/api/evenements/search")
    @Operation(summary = "Rechercher des événements", description = "Recherche d'événements avec filtrage par mot-clé et catégorie")
    
    public ResponseEntity<ApiResponse<Page<EvenementDto.PageResponse>>> searchEvenements(
            @Parameter(description = "Terme de recherche (titre, description)", example = "élections")
            @RequestParam(required = false) String query,
            @Parameter(description = "Filtrer par catégorie (slug)", example = "politique")
            @RequestParam(required = false) String categorie,
            @Parameter(description = "Numéro de la page (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre de résultats par page", example = "9")
            @RequestParam(defaultValue = "9") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                evenementService.search(query, categorie, page, size)));
    }

    @GetMapping("/api/evenements/{id}")
    @Operation(summary = "Détail d'un événement", description = "Récupère les informations complètes d'un événement")
    
    public ResponseEntity<ApiResponse<EvenementDto.Response>> getEvenement(
            @Parameter(description = "ID unique de l'événement", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(evenementService.getDetail(id)));
    }

    @PostMapping("/api/evenements")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Créer un événement", description = "Crée un nouvel événement (Admin seulement)")
    public ResponseEntity<ApiResponse<EvenementDto.Response>> createEvenement(
            @RequestBody(
                    description = "Données de l'événement à créer",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EvenementDto.Request.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody EvenementDto.Request req,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Événement créé",
                        evenementService.create(req, userId)));
    }

    @PutMapping("/api/evenements/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Modifier un événement", description = "Met à jour les informations d'un événement (Admin seulement)")
    
    public ResponseEntity<ApiResponse<EvenementDto.Response>> updateEvenement(
            @Parameter(description = "ID de l'événement à modifier", required = true)
            @PathVariable UUID id,
            @RequestBody(
                    description = "Nouvelles données de l'événement",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EvenementDto.Request.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody EvenementDto.Request req) {
        return ResponseEntity.ok(ApiResponse.ok(evenementService.update(id, req)));
    }

    @PatchMapping("/api/evenements/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Changer le statut d'un événement", description = "Change le statut d'un événement (OUVERT, FERMÉ, RÉSOLU) - Admin seulement")
    
    public ResponseEntity<ApiResponse<Void>> changerStatutEvenement(
            @Parameter(description = "ID de l'événement", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Nouveau statut: OUVERT, FERMÉ, RÉSOLU", required = true, example = "FERMÉ")
            @RequestParam Evenement.Statut statut) {
        evenementService.changerStatut(id, statut);
        return ResponseEntity.ok(ApiResponse.ok("Statut mis à jour", null));
    }

    // ═══════════════════════════════════════════════════════════════
    //  POSITIONS (MISES)
    // ═══════════════════════════════════════════════════════════════

    @PostMapping("/api/positions")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Placer une mise", description = "Place une mise OUI ou NON sur un marché d'un événement")

    public ResponseEntity<ApiResponse<PositionDto.Response>> placerMise(
            @RequestBody(
                    description = "Mise OUI ou NON sur un marché",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PositionDto.Request.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody PositionDto.Request req,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Mise enregistrée",
                        positionService.placerMise(req, userId)));
    }

    @GetMapping("/api/positions/mes-positions")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mes positions", description = "Récupère toutes les mises de l'utilisateur connecté")

    public ResponseEntity<ApiResponse<List<PositionDto.Response>>> getMesPositions(
            @RequestAttribute(value = "userId", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                positionService.getMesPositions(userId)));
    }

    @PostMapping("/api/marches/{marcheId}/resoudre")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Résoudre un marché", description = "Clôt un marché et distribue les gains aux gagnants (Admin seulement)")
    
    public ResponseEntity<ApiResponse<Void>> resoudreMarche(
            @Parameter(description = "ID unique du marché à résoudre", required = true)
            @PathVariable UUID marcheId,
            @RequestBody(
                    description = "Outcome gagnant : OUI ou NON",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MarcheDto.ResolveRequest.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody MarcheDto.ResolveRequest req) {
        positionService.resoudreMarche(marcheId, req.getOutcomeGagnant());
        return ResponseEntity.ok(ApiResponse.ok("Marché résolu, gains distribués", null));
    }


    // ═══════════════════════════════════════════════════════════════
    //  COMMENTAIRES
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/api/evenements/{evenementId}/commentaires")
    @Operation(summary = "Lister les commentaires d'un événement", description = "Récupère la liste paginée des commentaires d'un événement, avec compteur de likes")
    
    public ResponseEntity<ApiResponse<Page<CommentaireDto.Response>>> getCommentaires(
            @Parameter(description = "ID de l'événement", required = true)
            @PathVariable UUID evenementId,
            @Parameter(description = "Numéro de la page (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0")  int page,
            @Parameter(description = "Nombre de commentaires par page", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails user) {
        String tel = user != null ? user.getUsername() : null;
        return ResponseEntity.ok(ApiResponse.ok(
                commentaireService.getCommentaires(evenementId, page, size, tel)));
    }

    @PostMapping("/api/evenements/{evenementId}/commentaires")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Publier un commentaire", description = "Crée un nouveau commentaire sur un événement")
    
    public ResponseEntity<ApiResponse<CommentaireDto.Response>> publierCommentaire(
            @Parameter(description = "ID de l'événement", required = true)
            @PathVariable UUID evenementId,
            @RequestBody(
                    description = "Contenu du commentaire",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CommentaireDto.Request.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody CommentaireDto.Request req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Commentaire publié",
                        commentaireService.publier(evenementId, req, user.getUsername())));
    }

    @PostMapping("/api/evenements/{evenementId}/commentaires/{commentaireId}/like")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Basculer le like d'un commentaire", description = "Aime ou retire le like d'un commentaire")
    
    public ResponseEntity<ApiResponse<Integer>> toggleLike(
            @Parameter(description = "ID de l'événement", required = true)
            @PathVariable UUID evenementId,
            @Parameter(description = "ID du commentaire", required = true)
            @PathVariable UUID commentaireId,
            @AuthenticationPrincipal UserDetails user) {
        int nb = commentaireService.toggleLike(commentaireId, user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(nb));
    }

    @PostMapping("/api/evenements/{evenementId}/commentaires/{commentaireId}/signaler")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Signaler un commentaire", description = "Signale un commentaire pour modération")
    
    public ResponseEntity<ApiResponse<Void>> signalerCommentaire(
            @Parameter(description = "ID de l'événement", required = true)
            @PathVariable UUID evenementId,
            @Parameter(description = "ID du commentaire", required = true)
            @PathVariable UUID commentaireId,
            @AuthenticationPrincipal UserDetails user) {
        commentaireService.signaler(commentaireId, user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Commentaire signalé", null));
    }

    @DeleteMapping("/api/evenements/{evenementId}/commentaires/{commentaireId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Supprimer un commentaire", description = "Supprime un commentaire (l'auteur ou un admin peut le supprimer)")
    
    public ResponseEntity<ApiResponse<Void>> supprimerCommentaire(
            @Parameter(description = "ID de l'événement", required = true)
            @PathVariable UUID evenementId,
            @Parameter(description = "ID du commentaire", required = true)
            @PathVariable UUID commentaireId,
            @AuthenticationPrincipal UserDetails user) {
        commentaireService.supprimer(commentaireId, user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Commentaire supprimé", null));
    }

    // ═══════════════════════════════════════════════════════════════
    //  UTILISATEUR CONNECTÉ (profil + statistiques)
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/api/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Profil utilisateur connecté", description = "Récupère les informations de profil de l'utilisateur connecté")

    public ResponseEntity<ApiResponse<MeDto.MeResponse>> getMe(
            @RequestAttribute(value = "userId", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getMe(userId)));
    }

    @GetMapping("/api/me/stats")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Statistiques utilisateur", description = "Récupère les statistiques personnelles (mises, gains, pertes, taux de réussite)")

    public ResponseEntity<ApiResponse<MeDto.StatsResponse>> getMesStats(
            @RequestAttribute(value = "userId", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getStats(userId)));
    }

    // ═══════════════════════════════════════════════════════════════
    //  CLASSEMENT & ACTIVITÉ (publics)
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/api/leaderboard")
    @Operation(summary = "Classement des meilleures utilisateurs", description = "Récupère le top classement des utilisateurs (public, par performance)")
    
    public ResponseEntity<ApiResponse<List<AnalyticsDto.LeaderboardRow>>> leaderboard(
            @Parameter(description = "Nombre maximum de résultats à retourner", example = "50")
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getLeaderboard(limit)));
    }

    @GetMapping("/api/activite")
    @Operation(summary = "Dernière activité des utilisateurs", description = "Récupère le flux d'activité récente des utilisateurs (public)")
    
    public ResponseEntity<ApiResponse<List<AnalyticsDto.ActiviteRow>>> activite(
            @Parameter(description = "Nombre maximum d'activités à retourner", example = "20")
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getActivite(limit)));
    }

    // ═══════════════════════════════════════════════════════════════
    //  NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/api/notifications")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mes notifications", description = "Récupère les notifications non lues de l'utilisateur connecté")

    public ResponseEntity<ApiResponse<List<NotificationDto.Response>>> getNotifications(
            @RequestAttribute(value = "userId", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getMine(userId)));
    }

    @PatchMapping("/api/notifications/{id}/lu")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Marquer une notification comme lue", description = "Marque une notification comme lue pour l'utilisateur")

    public ResponseEntity<ApiResponse<Void>> markNotificationRead(
            @PathVariable UUID id,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        notificationService.markRead(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Notification marquée comme lue", null));
    }

    @GetMapping("/api/marches/{marcheId}/cagnotte")
    @Operation(summary = "Cagnotte d'un marché", description = "Récupère la répartition de la cagnotte entre OUI et NON, ainsi que le nombre de participants de chaque côté")
    public ResponseEntity<ApiResponse<MarcheDto.CagnotteResponse>> getCagnotte(
            @Parameter(description = "ID du marché", required = true)
            @PathVariable UUID marcheId) {
        return ResponseEntity.ok(ApiResponse.ok(positionService.getCagnotte(marcheId)));
    }
}