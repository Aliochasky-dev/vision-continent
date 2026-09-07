package com.VISION.continent.service;

import com.VISION.continent.dtos.WalletDto;
import com.VISION.continent.dtos.WalletTransactionDto;
import com.VISION.continent.entity.*;
import com.VISION.continent.execption.VisionException;
import com.VISION.continent.repository.WalletRepository;
import com.VISION.continent.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    /** Récupère le wallet d'un utilisateur, le crée s'il n'existe pas encore. */
    @Transactional
    public Wallet getOrCreateWallet(User user) {
        return walletRepository.findByUserId(user.getId())
                .orElseGet(() -> walletRepository.save(Wallet.builder().user(user).build()));
    }

    /**
     * Crée un mouvement EN_ATTENTE, sans toucher au solde.
     * Utilisé au moment de l'initiation d'un dépôt ou d'un retrait.
     */
    @Transactional
    public WalletTransaction creerMouvementEnAttente(Wallet wallet, WalletTransaction.Type type,
                                                     BigDecimal montant, WalletTransaction.Categorie categorie, String description) {

        WalletTransaction tx = WalletTransaction.builder()
                .wallet(wallet)
                .type(type)
                .categorie(categorie)
                .montantFcfa(montant)
                .soldeAvant(wallet.getSoldeFcfa())
                .soldeApres(wallet.getSoldeFcfa()) // pas encore appliqué
                .statut(WalletTransaction.Statut.EN_ATTENTE)
                .description(description)
                .build();

        return walletTransactionRepository.save(tx);
    }

    /**
     * Confirme un mouvement EN_ATTENTE : applique réellement le crédit/débit
     * sur le solde, avec verrou pessimiste pour éviter toute race condition.
     */
    @Transactional
    public WalletTransaction confirmerMouvement(UUID walletTransactionId) {
        WalletTransaction tx = walletTransactionRepository.findById(walletTransactionId)
                .orElseThrow(() -> new VisionException("Mouvement introuvable"));

        if (tx.getStatut() != WalletTransaction.Statut.EN_ATTENTE) {
            throw new VisionException("Ce mouvement a déjà été traité");
        }

        Wallet wallet = walletRepository.findByIdForUpdate(tx.getWallet().getId())
                .orElseThrow(() -> new VisionException("Wallet introuvable"));

        BigDecimal soldeAvant = wallet.getSoldeFcfa();
        BigDecimal soldeApres;

        if (tx.getType() == WalletTransaction.Type.CREDIT) {
            soldeApres = soldeAvant.add(tx.getMontantFcfa());
        } else {
            if (soldeAvant.compareTo(tx.getMontantFcfa()) < 0) {
                tx.setStatut(WalletTransaction.Statut.ECHOUE);
                walletTransactionRepository.save(tx);
                throw new VisionException("Solde insuffisant au moment de la confirmation");
            }
            soldeApres = soldeAvant.subtract(tx.getMontantFcfa());
        }

        wallet.setSoldeFcfa(soldeApres);
        walletRepository.save(wallet);

        tx.setSoldeAvant(soldeAvant);
        tx.setSoldeApres(soldeApres);
        tx.setStatut(WalletTransaction.Statut.CONFIRME);
        return walletTransactionRepository.save(tx);
    }

    /** Marque un mouvement EN_ATTENTE comme échoué, sans toucher au solde. */
    @Transactional
    public WalletTransaction echouerMouvement(UUID walletTransactionId) {
        WalletTransaction tx = walletTransactionRepository.findById(walletTransactionId)
                .orElseThrow(() -> new VisionException("Mouvement introuvable"));

        if (tx.getStatut() != WalletTransaction.Statut.EN_ATTENTE) {
            throw new VisionException("Ce mouvement a déjà été traité");
        }

        tx.setStatut(WalletTransaction.Statut.ECHOUE);
        return walletTransactionRepository.save(tx);
    }

    /**
     * Crédite directement le wallet (sans étape EN_ATTENTE) — pour les cas
     * internes comme les gains de résolution de marché ou les remboursements,
     * où il n'y a pas de confirmation externe à attendre.
     */
    @Transactional
    public WalletTransaction crediterDirect(Wallet wallet, BigDecimal montant,
                                            WalletTransaction.Categorie categorie, String description, Position position) {

        Wallet w = walletRepository.findByIdForUpdate(wallet.getId())
                .orElseThrow(() -> new VisionException("Wallet introuvable"));

        BigDecimal soldeAvant = w.getSoldeFcfa();
        BigDecimal soldeApres = soldeAvant.add(montant);
        w.setSoldeFcfa(soldeApres);
        walletRepository.save(w);

        WalletTransaction tx = WalletTransaction.builder()
                .wallet(w)
                .type(WalletTransaction.Type.CREDIT)
                .categorie(categorie)
                .montantFcfa(montant)
                .soldeAvant(soldeAvant)
                .soldeApres(soldeApres)
                .statut(WalletTransaction.Statut.CONFIRME)
                .description(description)
                .position(position)
                .build();

        return walletTransactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public WalletDto.Response toResponse(Wallet wallet) {
        return WalletDto.Response.builder()
                .id(wallet.getId())
                .soldeFcfa(wallet.getSoldeFcfa())
                .statut(wallet.getStatut())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<WalletTransactionDto.Response> getHistorique(UUID walletId) {
        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId)
                .stream().map(this::toTransactionResponse).collect(Collectors.toList());
    }

    private WalletTransactionDto.Response toTransactionResponse(WalletTransaction tx) {
        return WalletTransactionDto.Response.builder()
                .id(tx.getId())
                .type(tx.getType())
                .categorie(tx.getCategorie())
                .montantFcfa(tx.getMontantFcfa())
                .soldeAvant(tx.getSoldeAvant())
                .soldeApres(tx.getSoldeApres())
                .statut(tx.getStatut())
                .referenceExterne(tx.getReferenceExterne())
                .description(tx.getDescription())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}