package com.VISION.continent.repository;

import com.VISION.continent.entity.Position;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID> {

    // ← Long pour userId
    List<Position> findByUserId(Long userId);
    List<Position> findByUserIdOrderByCreatedAtDesc(Long userId);  // plus récentes d'abord
    List<Position> findByMarcheId(UUID marcheId);
    Optional<Position> findByUserIdAndMarcheId(Long userId, UUID marcheId);
    List<Position> findByMarcheIdAndChoix(UUID marcheId, Position.Choix choix);

    /** Classement par volume misé : [userId, prenom, nom, volumeFcfa, nbTrades]. */
    @Query("""
           select u.id, u.prenom, u.nom, coalesce(sum(p.montantMiseFcfa), 0), count(p)
           from Position p join p.user u
           group by u.id, u.prenom, u.nom
           order by sum(p.montantMiseFcfa) desc
           """)
    List<Object[]> leaderboardByVolume(Pageable pageable);

    /** Comptage des positions par utilisateur et statut : [userId, statut, count]. */
    @Query("select p.user.id, p.statut, count(p) from Position p group by p.user.id, p.statut")
    List<Object[]> countByUserAndStatut();

    /** Positions récentes, avec user + marché + événement déjà chargés (flux d'activité). */
    @Query("""
           select p from Position p
           join fetch p.user
           join fetch p.marche m
           join fetch m.evenement
           order by p.createdAt desc
           """)
    List<Position> findRecentWithDetails(Pageable pageable);
    boolean existsByUserIdAndMarche_EvenementId(Long userId, UUID evenementId);
}
