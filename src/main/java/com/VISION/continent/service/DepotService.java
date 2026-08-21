package com.VISION.continent.service;

import com.VISION.continent.dtos.DepotDto;
import com.VISION.continent.dtos.NokashDto;
import com.VISION.continent.entity.*;
import com.VISION.continent.execption.VisionException;
import com.VISION.continent.repository.PaymentIntentRepository;
import com.VISION.continent.repository.PaymentProviderRepository;
import com.VISION.continent.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepotService {

    private final WalletService walletService;
    private final UserRepository userRepository;
    private final PaymentProviderRepository paymentProviderRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final NokashPaymentService nokashPaymentService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Transactional
    public DepotDto.Response initierDepot(DepotDto.Request req, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));
        Wallet wallet = walletService.getOrCreateWallet(user);

        WalletTransaction tx = walletService.creerMouvementEnAttente(
                wallet,
                WalletTransaction.Type.CREDIT,
                req.getMontantFcfa(),
                WalletTransaction.Categorie.DEPOT,
                req.getDescription() != null ? req.getDescription() : "Dépôt Mobile Money");

        PaymentProvider provider = paymentProviderRepository.findByCode("NOKASH")
                .orElseThrow(() -> new VisionException("Fournisseur NOKASH non configuré"));

        String orderId = "ord_" + UUID.randomUUID().toString().replace("-", "");

        PaymentIntent intent = PaymentIntent.builder()
                .walletTransaction(tx)
                .provider(provider)
                .orderId(orderId)
                .montantFcfa(req.getMontantFcfa())
                .statut(PaymentIntent.Statut.INITIE)
                .build();
        intent = paymentIntentRepository.save(intent);

        // ═══════════════════════════════════════════════════════
        // C'EST ICI LA MODIFICATION : on passe req.getOperateur().name()
        // au lieu du "MTN_MOMO" codé en dur qu'on avait avant.
        // ═══════════════════════════════════════════════════════
        NokashDto.PayinResponse nokashResponse = nokashPaymentService.initierPayin(
                orderId, req.getMontantFcfa(), req.getTelephonePaiement(), req.getOperateur().name());

        intent.setProviderTransactionId(nokashResponse.getData().getId());
        intent.setStatut(PaymentIntent.Statut.EN_ATTENTE);
        try {
            intent.setPayloadReponse(objectMapper.writeValueAsString(nokashResponse));
        } catch (Exception ignored) {}
        paymentIntentRepository.save(intent);

        return DepotDto.Response.builder()
                .walletTransactionId(tx.getId())
                .paymentIntentId(intent.getId())
                .paymentUrl(null)
                .montantFcfa(req.getMontantFcfa())
                .statut(tx.getStatut().name())
                .build();
    }

    /** Conservé pour tests manuels admin ; le flux normal passera par le webhook. */
    @Transactional
    public void confirmerDepot(UUID walletTransactionId) {
        walletService.confirmerMouvement(walletTransactionId);
    }
}