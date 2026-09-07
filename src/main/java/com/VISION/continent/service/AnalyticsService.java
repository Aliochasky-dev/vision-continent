package com.VISION.continent.service;

import com.VISION.continent.dtos.AnalyticsDto;
import com.VISION.continent.entity.Marche;
import com.VISION.continent.entity.Position;
import com.VISION.continent.entity.Transaction;
import com.VISION.continent.repository.PositionRepository;
import com.VISION.continent.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PositionRepository positionRepository;
    private final TransactionRepository transactionRepository;

    /** Classement des traders par volume misé, avec profit (gains encaissés) et taux de victoire. */
    @Transactional(readOnly = true)
    public List<AnalyticsDto.LeaderboardRow> getLeaderboard(int limit) {
        List<Object[]> rows = positionRepository.leaderboardByVolume(PageRequest.of(0, Math.max(1, limit)));

        // Gains totaux par utilisateur (GAIN_RESOLUTION + REMBOURSEMENT)
        Map<Long, BigDecimal> gains = new HashMap<>();
        for (Object[] r : transactionRepository.sumByUserAndTypes(
                List.of(Transaction.TypeTransaction.GAIN_RESOLUTION, Transaction.TypeTransaction.REMBOURSEMENT))) {
            gains.put(((Number) r[0]).longValue(), toBig(r[1]));
        }

        // Victoires / défaites par utilisateur
        Map<Long, long[]> wl = new HashMap<>(); // [gagnees, perdues]
        for (Object[] r : positionRepository.countByUserAndStatut()) {
            Long uid = ((Number) r[0]).longValue();
            Position.Statut st = (Position.Statut) r[1];
            long cnt = ((Number) r[2]).longValue();
            long[] arr = wl.computeIfAbsent(uid, k -> new long[2]);
            if (st == Position.Statut.GAGNEE) arr[0] += cnt;
            else if (st == Position.Statut.PERDUE) arr[1] += cnt;
        }

        List<AnalyticsDto.LeaderboardRow> out = new ArrayList<>();
        int rang = 1;
        for (Object[] r : rows) {
            Long uid = ((Number) r[0]).longValue();
            String nomComplet = ((str(r[1]) + " " + str(r[2])).trim());
            BigDecimal volume = toBig(r[3]);
            long nbTrades = ((Number) r[4]).longValue();
            long[] w = wl.getOrDefault(uid, new long[2]);
            long resolues = w[0] + w[1];
            double taux = resolues > 0 ? (double) w[0] / resolues : 0.0;

            out.add(AnalyticsDto.LeaderboardRow.builder()
                    .rang(rang++)
                    .userId(uid)
                    .nomComplet(nomComplet)
                    .volumeFcfa(volume)
                    .profitFcfa(gains.getOrDefault(uid, BigDecimal.ZERO))
                    .nbTrades(nbTrades)
                    .tauxVictoire(taux)
                    .build());
        }
        return out;
    }

    /** Flux des mises récentes sur toute la plateforme. */
    @Transactional(readOnly = true)
    public List<AnalyticsDto.ActiviteRow> getActivite(int limit) {
        return positionRepository.findRecentWithDetails(PageRequest.of(0, Math.max(1, limit)))
                .stream().map(p -> {
                    Marche m = p.getMarche();
                    return AnalyticsDto.ActiviteRow.builder()
                            .positionId(p.getId())
                            .nomComplet((str(p.getUser().getPrenom()) + " " + str(p.getUser().getNom())).trim())
                            .evenementId(m.getEvenement().getId())
                            .evenementTitre(m.getEvenement().getTitre())
                            .marcheQuestion(m.getQuestion())
                            .choix(p.getChoix())
                            .montantFcfa(p.getMontantMiseFcfa())
                            .statut(p.getStatut())
                            .createdAt(p.getCreatedAt())
                            .build();
                }).collect(Collectors.toList());
    }

    private static BigDecimal toBig(Object o) {
        return o == null ? BigDecimal.ZERO : new BigDecimal(o.toString());
    }
    private static String str(Object o) { return o == null ? "" : o.toString(); }
}
