package com.VISION.continent.service;

import com.VISION.continent.dtos.PositionDto;
import com.VISION.continent.entity.*;
import com.VISION.continent.execption.VisionException;
import com.VISION.continent.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;
    private final MarcheRepository marcheRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;
    private final WalletService walletService;

    @Transactional
    public PositionDto.Response placerMise(PositionDto.Request req, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));


        Marche marche = marcheRepository.findById(req.getMarcheId())
                .orElseThrow(() -> new VisionException("Marché introuvable"));

        if (marche.getStatut() != Marche.Statut.OUVERT) {
            throw new VisionException("Ce marché est fermé");
        }
        if (marche.getEvenement().getStatut() != Evenement.Statut.OUVERT) {
            throw new VisionException("Cet événement est fermé");
        }
        if (marche.getEvenement().getDateFin().isBefore(LocalDateTime.now())) {
            throw new VisionException("La date limite de ce marché est dépassée");
        }

        Wallet wallet = walletService.getOrCreateWallet(user);
        if (wallet.getSoldeFcfa().compareTo(req.getMontantFcfa()) < 0) {
            throw new VisionException("Solde insuffisant. Solde actuel : " + wallet.getSoldeFcfa() + " FCFA");
        }

        Position position = Position.builder()
                .user(user)
                .marche(marche)
                .choix(req.getChoix())
                .montantMiseFcfa(req.getMontantFcfa())
                .statut(Position.Statut.ACTIVE)
                .build();
        position = positionRepository.save(position);

        // Débit direct du wallet (opération interne, pas de fournisseur externe à attendre)
        walletService.crediterDirect(wallet, req.getMontantFcfa().negate(),
                com.VISION.continent.entity.WalletTransaction.Categorie.MISE,
                "Mise sur \"" + marche.getQuestion() + "\"", position);

        transactionRepository.save(Transaction.builder()
                .user(user).position(position)
                .typeTx(Transaction.TypeTransaction.ACHAT_PARTS)
                .montantFcfa(req.getMontantFcfa())
                .statutPaiement(Transaction.StatutPaiement.CONFIRME)
                .build());

        if (req.getChoix() == Position.Choix.OUI) {
            marche.setPoolOuiFcfa(marche.getPoolOuiFcfa().add(req.getMontantFcfa()));
        } else {
            marche.setPoolNonFcfa(marche.getPoolNonFcfa().add(req.getMontantFcfa()));
        }
        marcheRepository.save(marche);

        return toResponse(position);
    }

    @Transactional
    public void resoudreMarche(UUID marcheId, String outcomeGagnant) {
        Marche marche = marcheRepository.findById(marcheId)
                .orElseThrow(() -> new VisionException("Marché introuvable"));

        if (marche.getStatut() != Marche.Statut.OUVERT) {
            throw new VisionException("Ce marché a déjà été résolu ou n'est plus ouvert");
        }

        Position.Choix choixGagnant = outcomeGagnant.equalsIgnoreCase("OUI")
                ? Position.Choix.OUI : Position.Choix.NON;

        BigDecimal poolGagnant = choixGagnant == Position.Choix.OUI
                ? marche.getPoolOuiFcfa() : marche.getPoolNonFcfa();
        BigDecimal poolTotal = marche.getPoolOuiFcfa().add(marche.getPoolNonFcfa());

        marche.setStatut(Marche.Statut.RESOLU);
        marche.setOutcomeGagnant(outcomeGagnant);
        marche.setResolvedAt(LocalDateTime.now());
        marcheRepository.save(marche);

        List<Position> positions = positionRepository.findByMarcheId(marcheId);

        if (poolGagnant.compareTo(BigDecimal.ZERO) == 0) {
            for (Position pos : positions) {
                User user = pos.getUser();
                Wallet wallet = walletService.getOrCreateWallet(user);
                walletService.crediterDirect(wallet, pos.getMontantMiseFcfa(),
                        com.VISION.continent.entity.WalletTransaction.Categorie.REMBOURSEMENT,
                        "Remboursement (aucun gagnant)", pos);

                transactionRepository.save(Transaction.builder()
                        .user(user).position(pos)
                        .typeTx(Transaction.TypeTransaction.REMBOURSEMENT)
                        .montantFcfa(pos.getMontantMiseFcfa())
                        .statutPaiement(Transaction.StatutPaiement.CONFIRME)
                        .build());

                pos.setStatut(Position.Statut.REMBOURSE);
                positionRepository.save(pos);

                notificationService.create(
                        user,
                        Notification.TypeNotif.REMBOURSEMENT,
                        "Mise remboursée",
                        "Votre mise de " + pos.getMontantMiseFcfa() + " FCFA a été remboursée (aucun gagnant)."
                );
            }
            return;
        }

        for (Position pos : positions) {
            User user = pos.getUser();

            if (pos.getChoix() == choixGagnant) {
                BigDecimal part = pos.getMontantMiseFcfa()
                        .divide(poolGagnant, 8, RoundingMode.HALF_UP);
                BigDecimal gain = part.multiply(poolTotal).setScale(2, RoundingMode.HALF_UP);

                Wallet wallet = walletService.getOrCreateWallet(user);
                walletService.crediterDirect(wallet, gain,
                        com.VISION.continent.entity.WalletTransaction.Categorie.GAIN_RESOLUTION,
                        "Gain sur \"" + marche.getQuestion() + "\"", pos);

                transactionRepository.save(Transaction.builder()
                        .user(user).position(pos)
                        .typeTx(Transaction.TypeTransaction.GAIN_RESOLUTION)
                        .montantFcfa(gain)
                        .statutPaiement(Transaction.StatutPaiement.CONFIRME)
                        .build());

                pos.setStatut(Position.Statut.GAGNEE);
                positionRepository.save(pos);

                notificationService.create(
                        user,
                        Notification.TypeNotif.GAIN,
                        "Félicitations ! Vous avez gagné",
                        "Vous avez gagné " + gain + " FCFA sur le marché \"" + marche.getQuestion() + "\"."
                );
            } else {
                pos.setStatut(Position.Statut.PERDUE);
                positionRepository.save(pos);

                notificationService.create(
                        user,
                        Notification.TypeNotif.PERTE,
                        "Résultat du marché",
                        "Votre position sur \"" + marche.getQuestion() + "\" a perdu."
                );
            }
        }
    }

    @Transactional(readOnly = true)
    public List<PositionDto.Response> getMesPositions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));
        return positionRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private PositionDto.Response toResponse(Position p) {
        Marche m = p.getMarche();
        Evenement ev = m.getEvenement();
        return PositionDto.Response.builder()
                .id(p.getId())
                .marcheId(m.getId())
                .marcheQuestion(m.getQuestion())
                .evenementId(ev.getId())
                .evenementTitre(ev.getTitre())
                .choix(p.getChoix())
                .montantMiseFcfa(p.getMontantMiseFcfa())
                .statut(p.getStatut())
                .createdAt(p.getCreatedAt())
                .build();
    }
}