package com.VISION.continent.controller;

import com.VISION.continent.dtos.*;
import com.VISION.continent.entity.*;
import com.VISION.continent.service.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class VisionController {

    private final CategorieService   categorieService;
    private final EvenementService   evenementService;
    private final PositionService    positionService;
    private final TransactionService transactionService;
    private final CommentaireService commentaireService;

    // ═══════════════════════════════════════════════════════════════
    //  CATÉGORIES
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/api/categories")
    public ResponseEntity<ApiResponse<List<CategorieDto.Response>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.ok(categorieService.findAll()));
    }

    @GetMapping("/api/categories/{slug}")
    public ResponseEntity<ApiResponse<CategorieDto.Response>> getCategorieBySlug(
            @PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(categorieService.findBySlug(slug)));
    }

    @PostMapping("/api/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategorieDto.Response>> createCategorie(
            @RequestBody(
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
    public ResponseEntity<ApiResponse<CategorieDto.Response>> updateCategorie(
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
    public ResponseEntity<ApiResponse<Void>> toggleCategorie(@PathVariable UUID id) {
        categorieService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ═══════════════════════════════════════════════════════════════
    //  ÉVÉNEMENTS
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/api/evenements")
    public ResponseEntity<ApiResponse<Page<EvenementDto.PageResponse>>> getCatalogue(
            @RequestParam(required = false)          String categorie,
            @RequestParam(defaultValue = "recent")   String sort,
            @RequestParam(defaultValue = "0")        int    page,
            @RequestParam(defaultValue = "9")        int    size) {
        return ResponseEntity.ok(ApiResponse.ok(
                evenementService.getCatalogue(categorie, sort, page, size)));
    }

    @GetMapping("/api/evenements/search")
    public ResponseEntity<ApiResponse<Page<EvenementDto.PageResponse>>> searchEvenements(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String categorie,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                evenementService.search(query, categorie, page, size)));
    }

    @GetMapping("/api/evenements/{id}")
    public ResponseEntity<ApiResponse<EvenementDto.Response>> getEvenement(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(evenementService.getDetail(id)));
    }

    @PostMapping("/api/evenements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EvenementDto.Response>> createEvenement(
            @RequestBody(
                    description = "Données de l'événement à créer",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EvenementDto.Request.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody EvenementDto.Request req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Événement créé",
                        evenementService.create(req, user.getUsername())));
    }

    @PutMapping("/api/evenements/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EvenementDto.Response>> updateEvenement(
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
    public ResponseEntity<ApiResponse<Void>> changerStatutEvenement(
            @PathVariable UUID id,
            @RequestParam Evenement.Statut statut) {
        evenementService.changerStatut(id, statut);
        return ResponseEntity.ok(ApiResponse.ok("Statut mis à jour", null));
    }

    // ═══════════════════════════════════════════════════════════════
    //  POSITIONS (MISES)
    // ═══════════════════════════════════════════════════════════════

    @PostMapping("/api/positions")
    public ResponseEntity<ApiResponse<PositionDto.Response>> placerMise(
            @RequestBody(
                    description = "Mise OUI ou NON sur un marché",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PositionDto.Request.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody PositionDto.Request req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Mise enregistrée",
                        positionService.placerMise(req, user.getUsername())));
    }

    @GetMapping("/api/positions/mes-positions")
    public ResponseEntity<ApiResponse<List<PositionDto.Response>>> getMesPositions(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(
                positionService.getMesPositions(user.getUsername())));
    }

    @PostMapping("/api/marches/{marcheId}/resoudre")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resoudreMarche(
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
    //  TRANSACTIONS (MOBILE MONEY)
    // ═══════════════════════════════════════════════════════════════

    @PostMapping("/api/transactions/depot")
    public ResponseEntity<ApiResponse<TransactionDto.Response>> depot(
            @RequestBody(
                    description = "Initier un dépôt Orange Money / MTN MoMo",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TransactionDto.DepotRequest.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody TransactionDto.DepotRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Dépôt en attente de confirmation",
                        transactionService.initierDepot(req, user.getUsername())));
    }

    @PostMapping("/api/transactions/depot/{id}/confirmer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> confirmerDepot(@PathVariable UUID id) {
        transactionService.confirmerDepot(id);
        return ResponseEntity.ok(ApiResponse.ok("Dépôt confirmé, solde crédité", null));
    }

    @PostMapping("/api/transactions/retrait")
    public ResponseEntity<ApiResponse<TransactionDto.Response>> retrait(
            @RequestBody(
                    description = "Initier un retrait vers Mobile Money",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TransactionDto.RetraitRequest.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody TransactionDto.RetraitRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Retrait initié",
                        transactionService.initierRetrait(req, user.getUsername())));
    }

    @GetMapping("/api/transactions/historique")
    public ResponseEntity<ApiResponse<List<TransactionDto.Response>>> historique(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(
                transactionService.getHistorique(user.getUsername())));
    }

    // ═══════════════════════════════════════════════════════════════
    //  COMMENTAIRES
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/api/evenements/{evenementId}/commentaires")
    public ResponseEntity<ApiResponse<Page<CommentaireDto.Response>>> getCommentaires(
            @PathVariable UUID evenementId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails user) {
        String tel = user != null ? user.getUsername() : null;
        return ResponseEntity.ok(ApiResponse.ok(
                commentaireService.getCommentaires(evenementId, page, size, tel)));
    }

    @PostMapping("/api/evenements/{evenementId}/commentaires")
    public ResponseEntity<ApiResponse<CommentaireDto.Response>> publierCommentaire(
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
    public ResponseEntity<ApiResponse<Integer>> toggleLike(
            @PathVariable UUID evenementId,
            @PathVariable UUID commentaireId,
            @AuthenticationPrincipal UserDetails user) {
        int nb = commentaireService.toggleLike(commentaireId, user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(nb));
    }

    @PostMapping("/api/evenements/{evenementId}/commentaires/{commentaireId}/signaler")
    public ResponseEntity<ApiResponse<Void>> signalerCommentaire(
            @PathVariable UUID evenementId,
            @PathVariable UUID commentaireId,
            @AuthenticationPrincipal UserDetails user) {
        commentaireService.signaler(commentaireId, user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Commentaire signalé", null));
    }

    @DeleteMapping("/api/evenements/{evenementId}/commentaires/{commentaireId}")
    public ResponseEntity<ApiResponse<Void>> supprimerCommentaire(
            @PathVariable UUID evenementId,
            @PathVariable UUID commentaireId,
            @AuthenticationPrincipal UserDetails user) {
        commentaireService.supprimer(commentaireId, user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Commentaire supprimé", null));
    }
}