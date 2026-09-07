package com.VISION.continent.repository;

import com.VISION.continent.entity.Commentaire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, UUID> {

    Page<Commentaire> findByEvenementIdOrderByCreatedAtDesc(UUID evenementId, Pageable pageable);
}