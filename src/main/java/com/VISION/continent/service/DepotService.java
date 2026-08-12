package com.VISION.continent.service;

import com.VISION.continent.dtos.DepotDto;
import com.VISION.continent.entity.*;
import com.VISION.continent.execption.VisionException;
import com.VISION.continent.repository.PaymentIntentRepository;
import com.VISION.continent.repository.PaymentProviderRepository;
import com.VISION.continent.repository.UserRepository;
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

    @Transactional
    public DepotDto.Response initierDepot(DepotDto.Request req, String telephone) {
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));
        Wallet wallet = walletService.getOrCreateWallet(user);

        WalletTransaction tx = walletService.creerMouvementEnAttente(
                wallet,
                WalletTransaction.Type.CREDIT,
                req.getMontantFcfa(),
                WalletTransaction.Categorie.DEPOT,
                req.getDescription() != null ? req.getDescription() : "Dépôt Mobile Money");

        PaymentProvider provider = paymentProviderRepository.findByCode("CINETPAY")
                .orElseThrow(() -> new VisionException("Fournisseur CINETPAY non configuré"));

        PaymentIntent intent = PaymentIntent.builder()
                .walletTransaction(tx)
                .provider(provider)
                .montantFcfa(req.getMontantFcfa())
                .statut(PaymentIntent.Statut.INITIE)
                .build();
        intent = paymentIntentRepository.save(intent);

        // TODO étape 5 : appel réel à CinetPay pour générer paymentUrl

        return DepotDto.Response.builder()
                .walletTransactionId(tx.getId())
                .paymentIntentId(intent.getId())
                .paymentUrl(null)
                .montantFcfa(req.getMontantFcfa())
                .statut(tx.getStatut().name())
                .build();
    }

    /** Temporaire (admin) : sera remplacé par le webhook CinetPay à l'étape 6. */
    @Transactional
    public void confirmerDepot(UUID walletTransactionId) {
        walletService.confirmerMouvement(walletTransactionId);
    }
}