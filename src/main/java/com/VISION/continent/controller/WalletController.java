package com.VISION.continent.controller;

import com.VISION.continent.dtos.*;
import com.VISION.continent.entity.User;
import com.VISION.continent.execption.ResourceNotFoundException;
import com.VISION.continent.execption.VisionException;
import com.VISION.continent.repository.UserRepository;
import com.VISION.continent.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet API", description = "Gestion du portefeuille et des transactions de dépôt/retrait")
public class WalletController {

    private final WalletService walletService;
    private final DepotService depotService;
    private final RetraitService retraitService;
    private final UserRepository userRepository;

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mon portefeuille", description = "Récupère les informations du portefeuille de l'utilisateur connecté (solde, date de création)")
    public ResponseEntity<ApiResponse<WalletDto.Response>> monWallet(
            @RequestAttribute(value = "userId", required = false) Long userId) {

        if (userId == null) {
            throw new VisionException("Utilisateur non authentifié");
        }

        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        var wallet = walletService.getOrCreateWallet(u);
        return ResponseEntity.ok(ApiResponse.ok(walletService.toResponse(wallet)));
    }

    @GetMapping("/transactions")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Historique des transactions du portefeuille", description = "Récupère l'historique complet des transactions du portefeuille (dépôts, retraits)")
    public ResponseEntity<ApiResponse<List<WalletTransactionDto.Response>>> historique(
            @RequestAttribute(value = "userId", required = false) Long userId) {

        if (userId == null) {
            throw new VisionException("Utilisateur non authentifié");
        }

        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        var wallet = walletService.getOrCreateWallet(u);
        return ResponseEntity.ok(ApiResponse.ok(walletService.getHistorique(wallet.getId())));
    }

    @PostMapping("/depot")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Initier un dépôt", description = "Initie un dépôt au portefeuille via Orange Money ou MTN MoMo en attente de confirmation admin")
    public ResponseEntity<ApiResponse<DepotDto.Response>> depot(
            @Valid @RequestBody DepotDto.Request req,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Dépôt initié", depotService.initierDepot(req, userId)));
    }

    @PostMapping("/depot/{walletTransactionId}/confirmer")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Confirmer un dépôt au portefeuille", description = "Confirme un dépôt en attente et crédite le solde du portefeuille (Admin seulement)")
    public ResponseEntity<ApiResponse<Void>> confirmerDepot(
            @Parameter(description = "ID de la transaction de dépôt au portefeuille", required = true)
            @PathVariable UUID walletTransactionId) {
        depotService.confirmerDepot(walletTransactionId);
        return ResponseEntity.ok(ApiResponse.ok("Dépôt confirmé, solde crédité", null));
    }

    @PostMapping("/retrait")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Initier un retrait", description = "Initie un retrait du portefeuille vers Mobile Money en attente de confirmation admin")
    public ResponseEntity<ApiResponse<RetraitDto.Response>> retrait(
            @Valid @RequestBody RetraitDto.Request req,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Retrait initié", retraitService.initierRetrait(req, userId)));
    }

    @PostMapping("/retrait/{walletTransactionId}/confirmer")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Confirmer un retrait du portefeuille", description = "Confirme un retrait en attente et débite le solde du portefeuille (Admin seulement)")
    public ResponseEntity<ApiResponse<Void>> confirmerRetrait(
            @Parameter(description = "ID de la transaction de retrait du portefeuille", required = true)
            @PathVariable UUID walletTransactionId) {
        retraitService.confirmerRetrait(walletTransactionId);
        return ResponseEntity.ok(ApiResponse.ok("Retrait confirmé, solde débité", null));
    }
}