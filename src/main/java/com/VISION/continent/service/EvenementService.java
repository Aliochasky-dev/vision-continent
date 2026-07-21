package com.VISION.continent.service;

import com.VISION.continent.dtos.*;
import com.VISION.continent.entity.*;
import com.VISION.continent.execption.VisionException;
import com.VISION.continent.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvenementService {

    private final EvenementRepository evenementRepository;
    private final CategorieRepository categorieRepository;
    private final MarcheRepository marcheRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<EvenementDto.PageResponse> getCatalogue(
            String categorieSlug, String sort, int page, int size) {

        Sort sorting = switch (sort != null ? sort : "recent") {
            case "fin"     -> Sort.by("dateFin").ascending();
            case "traders" -> Sort.by("nbTraders").descending();
            default        -> Sort.by("createdAt").descending();
        };

        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Evenement> result;
        if (categorieSlug != null && !categorieSlug.isBlank()) {
            Categorie cat = categorieRepository.findBySlug(categorieSlug)
                    .orElseThrow(() -> new VisionException("Catégorie introuvable"));
            result = evenementRepository.findByCategorieIdAndStatut(
                    cat.getId(), Evenement.Statut.OUVERT, pageable);
        } else {
            result = evenementRepository.findAll(pageable);
        }

        return result.map(this::toPageResponse);
    }

    @Transactional(readOnly = true)
    public EvenementDto.Response getDetail(UUID id) {
        Evenement ev = evenementRepository.findById(id)
                .orElseThrow(() -> new VisionException("Événement introuvable"));
        return toFullResponse(ev);
    }

    @Transactional(readOnly = true)
    public Page<EvenementDto.PageResponse> search(
            String query, String categorieSlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Evenement> result = evenementRepository.search(
                query != null && !query.isBlank() ? query : null,
                categorieSlug != null && !categorieSlug.isBlank() ? categorieSlug : null,
                pageable);
        return result.map(this::toPageResponse);
    }

    @Transactional(readOnly = true)
    public Page<EvenementDto.PageResponse> searchByTitre(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return evenementRepository.searchByTitre(query, pageable).map(this::toPageResponse);
    }

    @Transactional
    public EvenementDto.Response create(EvenementDto.Request req, String telephone) {
        User createur = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));
        Categorie cat = categorieRepository.findById(req.getCategorieId())
                .orElseThrow(() -> new VisionException("Catégorie introuvable"));

        if (req.getMarches() == null || req.getMarches().isEmpty()) {
            throw new VisionException("Un événement doit avoir au moins une option");
        }
        if (req.getTypeChoix() == Evenement.TypeChoix.UNIQUE && req.getMarches().size() != 1) {
            throw new VisionException("Un événement à choix unique doit avoir exactement 1 option");
        }

        Evenement ev = Evenement.builder()
                .titre(req.getTitre())
                .contexte(req.getContexte())
                .reglesResolution(req.getReglesResolution())
                .sourceResolution(req.getSourceResolution())
                .iconeUrl(req.getIconeUrl())
                .typeChoix(req.getTypeChoix())
                .categorie(cat)
                .createur(createur)
                .dateFin(req.getDateFin())
                .statut(Evenement.Statut.OUVERT)
                .build();

        ev = evenementRepository.save(ev);

        for (MarcheDto.Request mr : req.getMarches()) {
            Marche marche = Marche.builder()
                    .evenement(ev)
                    .question(mr.getQuestion())
                    .iconeUrl(mr.getIconeUrl())
                    .statut(Marche.Statut.OUVERT)
                    .build();
            marcheRepository.save(marche);
        }

        return toFullResponse(evenementRepository.findById(ev.getId()).get());
    }

    @Transactional
    public EvenementDto.Response update(UUID id, EvenementDto.Request req) {
        Evenement ev = evenementRepository.findById(id)
                .orElseThrow(() -> new VisionException("Événement introuvable"));

        ev.setTitre(req.getTitre());
        ev.setContexte(req.getContexte());
        ev.setReglesResolution(req.getReglesResolution());
        ev.setSourceResolution(req.getSourceResolution());
        ev.setIconeUrl(req.getIconeUrl());
        ev.setDateFin(req.getDateFin());

        return toFullResponse(evenementRepository.save(ev));
    }

    @Transactional
    public void changerStatut(UUID id, Evenement.Statut statut) {
        Evenement ev = evenementRepository.findById(id)
                .orElseThrow(() -> new VisionException("Événement introuvable"));
        ev.setStatut(statut);
        evenementRepository.save(ev);
    }

    private EvenementDto.PageResponse toPageResponse(Evenement ev) {
        List<MarcheDto.Response> marchesDto = marcheRepository.findByEvenementId(ev.getId())
                .stream().map(this::toMarcheResponse).collect(Collectors.toList());

        return EvenementDto.PageResponse.builder()
                .id(ev.getId())
                .titre(ev.getTitre())
                .iconeUrl(ev.getIconeUrl())
                .categorieNom(ev.getCategorie().getNom())
                .categorieSlug(ev.getCategorie().getSlug())
                .typeChoix(ev.getTypeChoix())
                .volumeTotalFcfa(ev.getVolumeTotalFcfa())
                .nbTraders(ev.getNbTraders())
                .dateFin(ev.getDateFin())
                .statut(ev.getStatut())
                .marches(marchesDto)
                .build();
    }

    private EvenementDto.Response toFullResponse(Evenement ev) {
        List<MarcheDto.Response> marchesDto = marcheRepository.findByEvenementId(ev.getId())
                .stream().map(this::toMarcheResponse).collect(Collectors.toList());

        CategorieDto.Response catDto = CategorieDto.Response.builder()
                .id(ev.getCategorie().getId())
                .nom(ev.getCategorie().getNom())
                .slug(ev.getCategorie().getSlug())
                .iconeUrl(ev.getCategorie().getIconeUrl())
                .build();

        return EvenementDto.Response.builder()
                .id(ev.getId())
                .titre(ev.getTitre())
                .contexte(ev.getContexte())
                .reglesResolution(ev.getReglesResolution())
                .sourceResolution(ev.getSourceResolution())
                .iconeUrl(ev.getIconeUrl())
                .categorie(catDto)
                .typeChoix(ev.getTypeChoix())
                .statut(ev.getStatut())
                .dateFin(ev.getDateFin())
                .dateResolution(ev.getDateResolution())
                .volumeTotalFcfa(ev.getVolumeTotalFcfa())
                .nbTraders(ev.getNbTraders())
                .createdAt(ev.getCreatedAt())
                .marches(marchesDto)
                .build();
    }

    private MarcheDto.Response toMarcheResponse(Marche m) {
        return MarcheDto.Response.builder()
                .id(m.getId())
                .question(m.getQuestion())
                .iconeUrl(m.getIconeUrl())
                .prixOui(m.getPrixOui())
                .prixNon(m.getPrixNon())
                .volumeFcfa(m.getVolumeFcfa())
                .statut(m.getStatut())
                .outcomeGagnant(m.getOutcomeGagnant())
                .build();
    }
}