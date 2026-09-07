package com.VISION.continent.repository;



import com.VISION.continent.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategorieRepository extends JpaRepository<Categorie, UUID> {
    List<Categorie> findByActiveTrue();
    Optional<Categorie> findBySlug(String slug);
}