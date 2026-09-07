package com.VISION.continent.service;

import com.VISION.continent.dtos.NokashDto;
import com.VISION.continent.dtos.RetraitDto;
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
public class RetraitService {

    private final WalletService walletService;
    private final UserRepository userRepository;
    private final PaymentProviderRepository paymentProviderRepository;
    private final NotificationService notificationService;
    private final PaymentIntentRepository paymentIntentRepository;
    private final NokashPayoutService nokashPayoutService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public RetraitDto.Response initierRetrait(RetraitDto.Request req, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));
        Wallet wallet = walletService.getOrCreateWallet(user);

        if (wallet.getSoldeFcfa().compareTo(req.getMontantFcfa()) < 0) {
            throw new VisionException("Solde insuffisant. Solde actuel : " + wallet.getSoldeFcfa() + " FCFA");
        }

        WalletTransaction tx = walletService.creerMouvementEnAttente(
                wallet,
                WalletTransaction.Type.DEBIT,
                req.getMontantFcfa(),
                WalletTransaction.Categorie.RETRAIT,
                "Retrait vers " + req.getTelephoneDestination());

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

        // Appel réel à Nokash (payout)
        NokashDto.PayoutResponse nokashResponse = nokashPayoutService.initierPayout(
                orderId, req.getMontantFcfa(), req.getTelephoneDestination(), req.getOperateur().name());

        intent.setProviderTransactionId(nokashResponse.getData().getId());
        intent.setStatut(PaymentIntent.Statut.EN_ATTENTE);
        try {
            intent.setPayloadReponse(objectMapper.writeValueAsString(nokashResponse));
        } catch (Exception ignored) {}
        paymentIntentRepository.save(intent);

        return RetraitDto.Response.builder()
                .walletTransactionId(tx.getId())
                .paymentIntentId(intent.getId())
                .montantFcfa(req.getMontantFcfa())
                .statut(tx.getStatut().name())
                .build();
    }

    /** Conservé pour tests manuels admin ; le flux normal passera par le webhook. */
    @Transactional
    public void confirmerRetrait(UUID walletTransactionId) {
        WalletTransaction tx = walletService.confirmerMouvement(walletTransactionId);

        notificationService.create(
                tx.getWallet().getUser(),
                Notification.TypeNotif.SYSTEME,
                "Retrait confirmé",
                "Votre retrait de " + tx.getMontantFcfa() + " FCFA a été traité avec succès."
        );
    }
}