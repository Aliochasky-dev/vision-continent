package com.VISION.continent.service;

import com.VISION.continent.dtos.TransactionDto;
import com.VISION.continent.entity.*;
import com.VISION.continent.execption.VisionException;
import com.VISION.continent.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /** Initier un dépôt Mobile Money (Orange/MTN) */
    @Transactional
    public TransactionDto.Response initierDepot(TransactionDto.DepotRequest req, String telephone) {
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));

        Transaction tx = Transaction.builder()
                .user(user)
                .typeTx(Transaction.TypeTransaction.DEPOT)
                .montantFcfa(req.getMontantFcfa())
                .operateur(req.getOperateur())
                .referenceMobileMoney(req.getReferenceMobileMoney())
                .statutPaiement(Transaction.StatutPaiement.EN_ATTENTE)
                .build();

        return toResponse(transactionRepository.save(tx));
    }

    /**
     * Webhook appelé par Orange Money / MTN pour confirmer un dépôt.
     * En prod : vérifier la signature du webhook avant de confirmer.
     */
    @Transactional
    public void confirmerDepot(UUID transactionId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new VisionException("Transaction introuvable"));

        if (tx.getStatutPaiement() != Transaction.StatutPaiement.EN_ATTENTE) {
            throw new VisionException("Transaction déjà traitée");
        }

        User user = tx.getUser();
        user.setSoldeFcfa(user.getSoldeFcfa().add(tx.getMontantFcfa()));
        userRepository.save(user);

        tx.setStatutPaiement(Transaction.StatutPaiement.CONFIRME);
        transactionRepository.save(tx);
    }

    /** Initier un retrait */
    @Transactional
    public TransactionDto.Response initierRetrait(TransactionDto.RetraitRequest req, String telephone) {
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));

        if (user.getSoldeFcfa().compareTo(req.getMontantFcfa()) < 0) {
            throw new VisionException("Solde insuffisant");
        }

        user.setSoldeFcfa(user.getSoldeFcfa().subtract(req.getMontantFcfa()));
        userRepository.save(user);

        Transaction tx = Transaction.builder()
                .user(user)
                .typeTx(Transaction.TypeTransaction.RETRAIT)
                .montantFcfa(req.getMontantFcfa())
                .operateur(req.getOperateur())
                .referenceMobileMoney(req.getNumeroCible())
                .statutPaiement(Transaction.StatutPaiement.EN_ATTENTE)
                .build();

        return toResponse(transactionRepository.save(tx));
    }

    /** Historique des transactions d'un utilisateur */
    public List<TransactionDto.Response> getHistorique(String telephone) {
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));

        // ← user.getId() retourne Long — le repository accepte maintenant Long
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private TransactionDto.Response toResponse(Transaction tx) {
        return TransactionDto.Response.builder()
                .id(tx.getId())
                .typeTx(tx.getTypeTx())
                .montantFcfa(tx.getMontantFcfa())
                .operateur(tx.getOperateur())
                .referenceMobileMoney(tx.getReferenceMobileMoney())
                .statutPaiement(tx.getStatutPaiement())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}