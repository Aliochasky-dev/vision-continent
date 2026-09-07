package com.VISION.continent.repository;

import com.VISION.continent.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, UUID> {

    // ← Long pour userId
    boolean existsByUserIdAndCommentaireId(Long userId, UUID commentaireId);
    void deleteByUserIdAndCommentaireId(Long userId, UUID commentaireId);
}