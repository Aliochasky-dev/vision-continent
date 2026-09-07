package com.VISION.continent.service;

import com.VISION.continent.dtos.NotificationDto;
import com.VISION.continent.entity.Notification;
import com.VISION.continent.entity.User;
import com.VISION.continent.execption.VisionException;
import com.VISION.continent.repository.NotificationRepository;
import com.VISION.continent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<NotificationDto.Response> getMine(Long userId) {
        User u = find(userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(u.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void markRead(UUID id, Long userId) {
        User u = find(userId);
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new VisionException("Notification introuvable"));
        if (!n.getUser().getId().equals(u.getId())) {
            throw new VisionException("Action non autorisée");
        }
        n.setLue(true);
        notificationRepository.save(n);
    }

    /** Utilitaire de création (réutilisable par d'autres services). */
    @Transactional
    public void create(User user, Notification.TypeNotif type, String titre, String message) {
        notificationRepository.save(Notification.builder()
                .user(user).typeNotif(type).titre(titre).message(message).lue(false).build());
    }

    private User find(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));
    }

    private NotificationDto.Response toResponse(Notification n) {
        return NotificationDto.Response.builder()
                .id(n.getId())
                .typeNotif(n.getTypeNotif())
                .titre(n.getTitre())
                .message(n.getMessage())
                .lue(Boolean.TRUE.equals(n.getLue()))
                .createdAt(n.getCreatedAt())
                .build();
    }
}