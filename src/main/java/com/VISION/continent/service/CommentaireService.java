package com.VISION.continent.service;

import com.VISION.continent.dtos.CommentaireDto;
import com.VISION.continent.entity.*;
import com.VISION.continent.execption.VisionException;
import com.VISION.continent.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentaireService {

    private final CommentaireRepository commentaireRepository;
    private final EvenementRepository evenementRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    @Transactional(readOnly = true)
    public Page<CommentaireDto.Response> getCommentaires(UUID evenementId, int page, int size, String telephone) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Commentaire> commentaires = commentaireRepository
                .findByEvenementIdOrderByCreatedAtDesc(evenementId, pageable);

        Long userId = telephone != null
                ? userRepository.findByTelephone(telephone).map(User::getId).orElse(null)
                : null;

        return commentaires.map(c -> toResponse(c, userId));
    }

    @Transactional
    public CommentaireDto.Response publier(UUID evenementId, CommentaireDto.Request req, String telephone) {
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));
        Evenement ev = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new VisionException("Événement introuvable"));

        Commentaire c = Commentaire.builder()
                .user(user)
                .evenement(ev)
                .contenu(req.getContenu())
                .build();

        return toResponse(commentaireRepository.save(c), user.getId());
    }

    @Transactional
    public int toggleLike(UUID commentaireId, String telephone) {
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));
        Commentaire c = commentaireRepository.findById(commentaireId)
                .orElseThrow(() -> new VisionException("Commentaire introuvable"));

        boolean alreadyLiked = likeRepository.existsByUserIdAndCommentaireId(user.getId(), commentaireId);
        if (alreadyLiked) {
            likeRepository.deleteByUserIdAndCommentaireId(user.getId(), commentaireId);
            c.setNbLikes(c.getNbLikes() - 1);
        } else {
            likeRepository.save(Like.builder().user(user).commentaire(c).build());
            c.setNbLikes(c.getNbLikes() + 1);
        }
        commentaireRepository.save(c);
        return c.getNbLikes();
    }

    @Transactional
    public void signaler(UUID commentaireId, String telephone) {
        Commentaire c = commentaireRepository.findById(commentaireId)
                .orElseThrow(() -> new VisionException("Commentaire introuvable"));
        c.setSignale(true);
        commentaireRepository.save(c);
    }

    @Transactional
    public void supprimer(UUID commentaireId, String telephone) {
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));
        Commentaire c = commentaireRepository.findById(commentaireId)
                .orElseThrow(() -> new VisionException("Commentaire introuvable"));

        boolean isAuteur = c.getUser().getId().equals(user.getId());
        boolean isAdmin  = user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.MODERATEUR;

        if (!isAuteur && !isAdmin) {
            throw new VisionException("Action non autorisée");
        }
        commentaireRepository.delete(c);
    }

    private CommentaireDto.Response toResponse(Commentaire c, Long currentUserId) {
        boolean likeParMoi = currentUserId != null
                && likeRepository.existsByUserIdAndCommentaireId(currentUserId, c.getId());

        return CommentaireDto.Response.builder()
                .id(c.getId())
                .auteurId(c.getUser().getId())
                .auteurNom(c.getUser().getPrenom() + " " + c.getUser().getNom())
                .contenu(c.getContenu())
                .nbLikes(c.getNbLikes())
                .likeParMoi(likeParMoi)
                .createdAt(c.getCreatedAt())
                .build();
    }
}