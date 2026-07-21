package com.VISION.continent.repository;


import com.VISION.continent.entity.Marche;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MarcheRepository extends JpaRepository<Marche, UUID> {
    List<Marche> findByEvenementId(UUID evenementId);
    List<Marche> findByEvenementIdAndStatut(UUID evenementId, Marche.Statut statut);
}
