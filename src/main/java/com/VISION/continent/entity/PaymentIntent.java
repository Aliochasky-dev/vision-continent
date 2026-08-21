package com.VISION.continent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_intents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentIntent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_transaction_id", nullable = false, unique = true)
    private WalletTransaction walletTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private PaymentProvider provider;

    // ─── NOUVEAU ─────────────────────────────────────────────
    // Généré par nous à l'initiation, envoyé à Nokash en "order_id".
    // C'est la clé de rapprochement quand le callback revient,
    // car le callback ne connaît pas notre UUID interne.
    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "provider_transaction_id")
    private String providerTransactionId;

    @Column(name = "montant_fcfa", precision = 15, scale = 2, nullable = false)
    private BigDecimal montantFcfa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Statut statut = Statut.INITIE;

    @Column(name = "payload_reponse", columnDefinition = "TEXT")
    private String payloadReponse;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (statut == null) statut = Statut.INITIE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Statut { INITIE, EN_ATTENTE, REUSSI, ECHOUE, EXPIRE }
}