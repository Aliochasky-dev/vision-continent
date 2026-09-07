package com.VISION.continent.service;

import com.VISION.continent.dtos.MeDto;
import com.VISION.continent.entity.Position;
import com.VISION.continent.entity.Transaction;
import com.VISION.continent.entity.User;
import com.VISION.continent.entity.Wallet;
import com.VISION.continent.execption.VisionException;
import com.VISION.continent.repository.PositionRepository;
import com.VISION.continent.repository.TransactionRepository;
import com.VISION.continent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PositionRepository positionRepository;
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;

    /** Profil de l'utilisateur connecté — permet de rafraîchir le solde à tout moment. */
    @Transactional(readOnly = true)
    public MeDto.MeResponse getMe(Long userId) {
        User u = find(userId);
        Wallet wallet = walletService.getOrCreateWallet(u);
        return MeDto.MeResponse.builder()
                .id(u.getId())
                .nomComplet(fullName(u))
                .telephone(u.getTelephone())
                .email(u.getEmail())
                .role(u.getRole())
                .soldeFcfa(wallet.getSoldeFcfa())
                .build();
    }

    /** Statistiques agrégées de l'utilisateur (page Profil). */
    @Transactional(readOnly = true)
    public MeDto.StatsResponse getStats(Long userId) {
        User u = find(userId);
        Wallet wallet = walletService.getOrCreateWallet(u);
        List<Position> positions = positionRepository.findByUserId(u.getId());

        BigDecimal volume = positions.stream()
                .map(Position::getMontantMiseFcfa)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long nbActives = positions.stream().filter(p -> p.getStatut() == Position.Statut.ACTIVE).count();
        long nbGagnees = positions.stream().filter(p -> p.getStatut() == Position.Statut.GAGNEE).count();
        long nbPerdues = positions.stream().filter(p -> p.getStatut() == Position.Statut.PERDUE).count();

        BigDecimal misesResolues = positions.stream()
                .filter(p -> p.getStatut() != Position.Statut.ACTIVE)
                .map(Position::getMontantMiseFcfa)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gains = transactionRepository.findByUserIdOrderByCreatedAtDesc(u.getId()).stream()
                .filter(t -> t.getTypeTx() == Transaction.TypeTransaction.GAIN_RESOLUTION
                        || t.getTypeTx() == Transaction.TypeTransaction.REMBOURSEMENT)
                .map(Transaction::getMontantFcfa)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long resolues = nbGagnees + nbPerdues;
        double taux = resolues > 0 ? (double) nbGagnees / resolues : 0.0;

        return MeDto.StatsResponse.builder()
                .soldeFcfa(wallet.getSoldeFcfa())
                .pnlTotalFcfa(gains.subtract(misesResolues))
                .volumeTradeFcfa(volume)
                .nbPositions(positions.size())
                .nbActives(nbActives)
                .nbGagnees(nbGagnees)
                .nbPerdues(nbPerdues)
                .tauxVictoire(taux)
                .membreDepuis(u.getCreatedAt())
                .build();
    }
    private User find(Long userId) {
        if (userId == null) {
            throw new VisionException("Utilisateur non authentifié");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));
    }

    private String fullName(User u) {
        return ((u.getPrenom() == null ? "" : u.getPrenom()) + " "
                + (u.getNom() == null ? "" : u.getNom())).trim();
    }
}