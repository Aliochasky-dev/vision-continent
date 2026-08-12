package com.VISION.continent.service;

import com.VISION.continent.dtos.TransactionDto;
import com.VISION.continent.entity.*;
import com.VISION.continent.execption.VisionException;
import com.VISION.continent.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

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
                .referenceMobileMoney(req.getContact())
                .statutPaiement(Transaction.StatutPaiement.EN_ATTENTE)
                .build();

        Transaction saved = transactionRepository.save(tx);
        log.info("\n╔══════════════ DÉPÔT EN ATTENTE ══════════════\n"
               + "║ id        : {}\n"
               + "║ montant   : {} FCFA   utilisateur : {}\n"
               + "║ CONFIRMER : POST /api/transactions/depot/{}/confirmer   (ADMIN)\n"
               + "╚═══════════════════════════════════════════════",
                saved.getId(), saved.getMontantFcfa(), telephone, saved.getId());
        return toResponse(saved);
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

        // === Notification ===
        notificationService.create(
                user,
                Notification.TypeNotif.DEPOT_CONFIRME,          // adapte selon ton enum
                "Dépôt confirmé",
                "Votre dépôt de " + tx.getMontantFcfa() + " FCFA a été crédité sur votre solde."
        );
    }
    /**
     * Initier un retrait. Le solde n'est PAS débité ici : il ne le sera qu'à la
     * confirmation par l'opérateur (voir confirmerRetrait), pour éviter de
     * débiter un retrait qui échoue côté Mobile Money.
     */
    @Transactional
    public TransactionDto.Response initierRetrait(TransactionDto.RetraitRequest req, String telephone) {
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));

        if (user.getSoldeFcfa().compareTo(req.getMontantFcfa()) < 0) {
            throw new VisionException("Solde insuffisant");
        }

        Transaction tx = Transaction.builder()
                .user(user)
                .typeTx(Transaction.TypeTransaction.RETRAIT)
                .montantFcfa(req.getMontantFcfa())
                .operateur(req.getOperateur())
                .referenceMobileMoney(req.getContact())
                .statutPaiement(Transaction.StatutPaiement.EN_ATTENTE)
                .build();

        Transaction saved = transactionRepository.save(tx);
        log.info("\n╔══════════════ RETRAIT EN ATTENTE ══════════════\n"
               + "║ id        : {}\n"
               + "║ montant   : {} FCFA   utilisateur : {}\n"
               + "║ CONFIRMER : POST /api/transactions/retrait/{}/confirmer   (ADMIN)\n"
               + "╚════════════════════════════════════════════════",
                saved.getId(), saved.getMontantFcfa(), telephone, saved.getId());
        return toResponse(saved);
    }

    /**
     * Confirmer un retrait (webhook opérateur / admin) : c'est ici que le solde
     * est réellement débité, après re-vérification du solde disponible.
     */
    @Transactional
    public void confirmerRetrait(UUID transactionId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new VisionException("Transaction introuvable"));

        if (tx.getTypeTx() != Transaction.TypeTransaction.RETRAIT) {
            throw new VisionException("Cette transaction n'est pas un retrait");
        }
        if (tx.getStatutPaiement() != Transaction.StatutPaiement.EN_ATTENTE) {
            throw new VisionException("Transaction déjà traitée");
        }

        User user = tx.getUser();
        if (user.getSoldeFcfa().compareTo(tx.getMontantFcfa()) < 0) {
            tx.setStatutPaiement(Transaction.StatutPaiement.ECHOUE);
            transactionRepository.save(tx);
            throw new VisionException("Solde insuffisant au moment de la confirmation");
        }

        user.setSoldeFcfa(user.getSoldeFcfa().subtract(tx.getMontantFcfa()));
        userRepository.save(user);

        tx.setStatutPaiement(Transaction.StatutPaiement.CONFIRME);
        transactionRepository.save(tx);
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