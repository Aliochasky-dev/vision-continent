package com.VISION.continent.controller;

import com.VISION.continent.dtos.*;
import com.VISION.continent.entity.User;
import com.VISION.continent.execption.VisionException;
import com.VISION.continent.repository.UserRepository;
import com.VISION.continent.service.*;
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
public class WalletController {

    private final WalletService walletService;
    private final DepotService depotService;
    private final RetraitService retraitService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<WalletDto.Response>> monWallet(@AuthenticationPrincipal UserDetails user) {
        User u = userRepository.findByTelephone(user.getUsername())
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));
        var wallet = walletService.getOrCreateWallet(u);
        return ResponseEntity.ok(ApiResponse.ok(walletService.toResponse(wallet)));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<WalletTransactionDto.Response>>> historique(
            @AuthenticationPrincipal UserDetails user) {
        User u = userRepository.findByTelephone(user.getUsername())
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));
        var wallet = walletService.getOrCreateWallet(u);
        return ResponseEntity.ok(ApiResponse.ok(walletService.getHistorique(wallet.getId())));
    }

    @PostMapping("/depot")
    public ResponseEntity<ApiResponse<DepotDto.Response>> depot(
            @Valid @RequestBody DepotDto.Request req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Dépôt initié", depotService.initierDepot(req, user.getUsername())));
    }

    @PostMapping("/depot/{walletTransactionId}/confirmer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> confirmerDepot(@PathVariable UUID walletTransactionId) {
        depotService.confirmerDepot(walletTransactionId);
        return ResponseEntity.ok(ApiResponse.ok("Dépôt confirmé, solde crédité", null));
    }

    @PostMapping("/retrait")
    public ResponseEntity<ApiResponse<RetraitDto.Response>> retrait(
            @Valid @RequestBody RetraitDto.Request req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Retrait initié", retraitService.initierRetrait(req, user.getUsername())));
    }

    @PostMapping("/retrait/{walletTransactionId}/confirmer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> confirmerRetrait(@PathVariable UUID walletTransactionId) {
        retraitService.confirmerRetrait(walletTransactionId);
        return ResponseEntity.ok(ApiResponse.ok("Retrait confirmé, solde débité", null));
    }
}