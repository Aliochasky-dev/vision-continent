package com.VISION.continent.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ← ManyToOne vers User (id = Long/bigint)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_notif", nullable = false)
    private TypeNotif typeNotif;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Boolean lue = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TypeNotif { RESOLUTION, GAIN, REPONSE, DEPOT_CONFIRME, SYSTEME }
}