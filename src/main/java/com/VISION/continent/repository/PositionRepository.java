package com.VISION.continent.repository;

import com.VISION.continent.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID> {

    // ← Long pour userId
    List<Position> findByUserId(Long userId);
    List<Position> findByMarcheId(UUID marcheId);
    Optional<Position> findByUserIdAndMarcheId(Long userId, UUID marcheId);
    List<Position> findByMarcheIdAndChoix(UUID marcheId, Position.Choix choix);
}