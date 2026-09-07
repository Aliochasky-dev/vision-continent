package com.VISION.continent.controller;

import com.VISION.continent.dtos.ApiResponse;
import com.VISION.continent.dtos.NokashDto;
import com.VISION.continent.entity.*;
import com.VISION.continent.repository.PaymentIntentRepository;
import com.VISION.continent.repository.PaymentProviderRepository;
import com.VISION.continent.repository.WebhookLogRepository;
import com.VISION.continent.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Callbacks des fournisseurs de paiement")
public class WebhookController {

    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentProviderRepository paymentProviderRepository;
    private final WebhookLogRepository webhookLogRepository;
    private final WalletService walletService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/nokash", consumes = "application/json")
    @Operation(summary = "Callback Nokash", description = "Endpoint public appelé par Nokash après traitement d'un paiement")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> nokashCallback(@RequestBody NokashDto.Callback callback) {

        PaymentProvider provider = paymentProviderRepository.findByCode("NOKASH").orElse(null);

        String rawPayload;
        try {
            rawPayload = objectMapper.writeValueAsString(callback);
        } catch (Exception e) {
            rawPayload = callback.toString();
        }

        WebhookLog log = WebhookLog.builder()
                .provider(provider)
                .payloadBrut(rawPayload)
                .signatureValide(true)
                .traite(false)
                .build();

        var intentOpt = paymentIntentRepository.findByOrderId(callback.getOrderId());
        if (intentOpt.isEmpty()) {
            webhookLogRepository.save(log);
            return ResponseEntity.ok(ApiResponse.error("orderId inconnu"));
        }

        PaymentIntent intent = intentOpt.get();

        if (intent.getStatut() == PaymentIntent.Statut.REUSSI
                || intent.getStatut() == PaymentIntent.Statut.ECHOUE) {
            log.setTraite(true);
            webhookLogRepository.save(log);
            return ResponseEntity.ok(ApiResponse.ok("Déjà traité", null));
        }

        boolean montantValide = intent.getMontantFcfa().compareTo(callback.getAmount()) == 0;

        if (!montantValide) {
            intent.setStatut(PaymentIntent.Statut.ECHOUE);
            paymentIntentRepository.save(intent);
            walletService.echouerMouvement(intent.getWalletTransaction().getId());
            log.setTraite(true);
            webhookLogRepository.save(log);
            return ResponseEntity.ok(ApiResponse.error("Montant incohérent"));
        }

        if ("SUCCESS".equalsIgnoreCase(callback.getStatus())) {
            intent.setStatut(PaymentIntent.Statut.REUSSI);
            paymentIntentRepository.save(intent);
            walletService.confirmerMouvement(intent.getWalletTransaction().getId());
        } else {
            intent.setStatut(PaymentIntent.Statut.ECHOUE);
            paymentIntentRepository.save(intent);
            walletService.echouerMouvement(intent.getWalletTransaction().getId());
        }

        log.setTraite(true);
        webhookLogRepository.save(log);

        return ResponseEntity.ok(ApiResponse.ok("Callback traité", null));
    }
}