package com.VISION.continent.service;

import com.VISION.continent.dtos.RetraitDto;
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
public class RetraitService {

    private final WalletService walletService;
    private final UserRepository userRepository;
    private final PaymentProviderRepository paymentProviderRepository;
    private final PaymentIntentRepository paymentIntentRepository;

    @Transactional
    public RetraitDto.Response initierRetrait(RetraitDto.Request req, String telephone) {
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));
        Wallet wallet = walletService.getOrCreateWallet(user);

        // Vérification anticipée pour un retour rapide à l'utilisateur ;
        // la vraie vérification (avec verrou) a lieu à la confirmation.
        if (wallet.getSoldeFcfa().compareTo(req.getMontantFcfa()) < 0) {
            throw new VisionException("Solde insuffisant. Solde actuel : " + wallet.getSoldeFcfa() + " FCFA");
        }

        WalletTransaction tx = walletService.creerMouvementEnAttente(
                wallet,
                WalletTransaction.Type.DEBIT,
                req.getMontantFcfa(),
                WalletTransaction.Categorie.RETRAIT,
                "Retrait vers " + req.getTelephoneDestination());

        PaymentProvider provider = paymentProviderRepository.findByCode("CINETPAY")
                .orElseThrow(() -> new VisionException("Fournisseur CINETPAY non configuré"));

        PaymentIntent intent = PaymentIntent.builder()
                .walletTransaction(tx)
                .provider(provider)
                .montantFcfa(req.getMontantFcfa())
                .statut(PaymentIntent.Statut.INITIE)
                .build();
        intent = paymentIntentRepository.save(intent);

        return RetraitDto.Response.builder()
                .walletTransactionId(tx.getId())
                .paymentIntentId(intent.getId())
                .montantFcfa(req.getMontantFcfa())
                .statut(tx.getStatut().name())
                .build();
    }

    /** Temporaire (admin) : sera remplacé par le webhook CinetPay à l'étape 6. */
    @Transactional
    public void confirmerRetrait(UUID walletTransactionId) {
        walletService.confirmerMouvement(walletTransactionId);
    }
}