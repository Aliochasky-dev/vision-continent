package com.VISION.continent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "webhook_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private PaymentProvider provider;

    @Column(name = "payload_brut", columnDefinition = "TEXT", nullable = false)
    private String payloadBrut;

    @Column(name = "signature_valide", nullable = false)
    @Builder.Default
    private Boolean signatureValide = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean traite = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (signatureValide == null) signatureValide = false;
        if (traite == null) traite = false;
    }
}