package com.VISION.continent.service;

import com.VISION.continent.dtos.CategorieDto;
import com.VISION.continent.entity.Categorie;
import com.VISION.continent.entity.Evenement;
import com.VISION.continent.execption.VisionException;
import com.VISION.continent.repository.CategorieRepository;
import com.VISION.continent.repository.EvenementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategorieService {

    private final CategorieRepository categorieRepository;
    private final EvenementRepository evenementRepository;

    public List<CategorieDto.Response> findAll() {
        return categorieRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CategorieDto.Response findBySlug(String slug) {
        Categorie cat = categorieRepository.findBySlug(slug)
                .orElseThrow(() -> new VisionException("Catégorie introuvable : " + slug));
        return toResponse(cat);
    }

    @Transactional
    public CategorieDto.Response create(CategorieDto.Request req) {
        Categorie cat = Categorie.builder()
                .nom(req.getNom())
                .slug(req.getSlug())
                .description(req.getDescription())
                .iconeUrl(req.getIconeUrl())
                .active(true)
                .build();
        return toResponse(categorieRepository.save(cat));
    }

    @Transactional
    public CategorieDto.Response update(UUID id, CategorieDto.Request req) {
        Categorie cat = categorieRepository.findById(id)
                .orElseThrow(() -> new VisionException("Catégorie introuvable"));
        cat.setNom(req.getNom());
        cat.setSlug(req.getSlug());
        cat.setDescription(req.getDescription());
        cat.setIconeUrl(req.getIconeUrl());
        return toResponse(categorieRepository.save(cat));
    }

    @Transactional
    public void toggleActive(UUID id) {
        Categorie cat = categorieRepository.findById(id)
                .orElseThrow(() -> new VisionException("Catégorie introuvable"));
        cat.setActive(!cat.getActive());
        categorieRepository.save(cat);
    }

    private CategorieDto.Response toResponse(Categorie cat) {
        long nb = evenementRepository.countByCategorieIdAndStatut(cat.getId(), Evenement.Statut.OUVERT);
        return CategorieDto.Response.builder()
                .id(cat.getId())
                .nom(cat.getNom())
                .slug(cat.getSlug())
                .description(cat.getDescription())
                .iconeUrl(cat.getIconeUrl())
                .active(cat.getActive())
                .nbMarches(nb)
                .build();
    }
}