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

    @Transactional
    public PositionDto.Response placerMise(PositionDto.Request req, String telephone) {
        User user = userRepository.findByTelephone(telephone)
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
        if (user.getSoldeFcfa().compareTo(req.getMontantFcfa()) < 0) {
            throw new VisionException("Solde insuffisant. Solde actuel : " + user.getSoldeFcfa() + " FCFA");
        }

        Position position = Position.builder()
                .user(user)
                .marche(marche)
                .choix(req.getChoix())
                .montantMiseFcfa(req.getMontantFcfa())
                .statut(Position.Statut.ACTIVE)
                .build();
        position = positionRepository.save(position);

        user.setSoldeFcfa(user.getSoldeFcfa().subtract(req.getMontantFcfa()));
        userRepository.save(user);

        transactionRepository.save(Transaction.builder()
                .user(user).position(position)
                .typeTx(Transaction.TypeTransaction.ACHAT_PARTS)
                .montantFcfa(req.getMontantFcfa())
                .statutPaiement(Transaction.StatutPaiement.CONFIRME)
                .build());

        // Alimente le bon pool — c'est ce qui fait bouger le prix automatiquement
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

        // Cas particulier : personne n'a misé sur le côté gagnant → rembourser tout le monde
        if (poolGagnant.compareTo(BigDecimal.ZERO) == 0) {
            for (Position pos : positions) {
                User user = pos.getUser();
                user.setSoldeFcfa(user.getSoldeFcfa().add(pos.getMontantMiseFcfa()));
                userRepository.save(user);

                transactionRepository.save(Transaction.builder()
                        .user(user).position(pos)
                        .typeTx(Transaction.TypeTransaction.REMBOURSEMENT)
                        .montantFcfa(pos.getMontantMiseFcfa())
                        .statutPaiement(Transaction.StatutPaiement.CONFIRME)
                        .build());

                pos.setStatut(Position.Statut.REMBOURSE);
                positionRepository.save(pos);

                // Notification remboursement
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

                user.setSoldeFcfa(user.getSoldeFcfa().add(gain));
                userRepository.save(user);

                transactionRepository.save(Transaction.builder()
                        .user(user).position(pos)
                        .typeTx(Transaction.TypeTransaction.GAIN_RESOLUTION)
                        .montantFcfa(gain)
                        .statutPaiement(Transaction.StatutPaiement.CONFIRME)
                        .build());

                pos.setStatut(Position.Statut.GAGNEE);
                positionRepository.save(pos);

                // Notification gain
                notificationService.create(
                        user,
                        Notification.TypeNotif.GAIN,
                        "Félicitations ! Vous avez gagné",
                        "Vous avez gagné " + gain + " FCFA sur le marché \"" + marche.getQuestion() + "\"."
                );
            } else {
                pos.setStatut(Position.Statut.PERDUE);
                positionRepository.save(pos);

                // Notification perte (optionnel mais recommandé)
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
    public List<PositionDto.Response> getMesPositions(String telephone) {
        User user = userRepository.findByTelephone(telephone)
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