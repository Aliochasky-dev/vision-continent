package com.VISION.continent.repository;



import com.VISION.continent.entity.Evenement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface EvenementRepository extends JpaRepository<Evenement, UUID> {

    // Recherche par catégorie
    Page<Evenement> findByCategorieIdAndStatut(UUID categorieId, Evenement.Statut statut, Pageable pageable);

    // Recherche full-text par titre
    @Query("SELECT e FROM Evenement e WHERE LOWER(e.titre) LIKE LOWER(CONCAT('%', :query, '%')) AND e.statut = 'OUVERT'")
    Page<Evenement> searchByTitre(@Param("query") String query, Pageable pageable);

    // Recherche par catégorie slug + titre
    @Query("""
        SELECT e FROM Evenement e
        JOIN e.categorie c
        WHERE (:query IS NULL OR LOWER(e.titre) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:slug IS NULL OR c.slug = :slug)
        AND e.statut = 'OUVERT'
        """)
    Page<Evenement> search(@Param("query") String query, @Param("slug") String slug, Pageable pageable);

    // Tous les marchés ouverts triés par volume
    List<Evenement> findByStatutOrderByVolumeTotalFcfaDesc(Evenement.Statut statut);

    long countByCategorieIdAndStatut(UUID categorieId, Evenement.Statut statut);
}
