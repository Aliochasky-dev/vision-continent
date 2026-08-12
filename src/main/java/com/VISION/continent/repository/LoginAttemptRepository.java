package com.VISION.continent.repository;

import com.VISION.continent.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    Optional<LoginAttempt> findByIdentifier(String identifier);

    @Modifying
    @Query("DELETE FROM LoginAttempt la WHERE la.dernierEchec < :seuil")
    void deleteOldAttempts(LocalDateTime seuil);
}