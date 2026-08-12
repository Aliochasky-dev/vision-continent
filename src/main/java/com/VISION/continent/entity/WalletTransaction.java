package com.VISION.continent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categorie categorie;

    @Column(name = "montant_fcfa", precision = 15, scale = 2, nullable = false)
    private BigDecimal montantFcfa;

    @Column(name = "solde_avant", precision = 15, scale = 2, nullable = false)
    private BigDecimal soldeAvant;

    @Column(name = "solde_apres", precision = 15, scale = 2, nullable = false)
    private BigDecimal soldeApres;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Statut statut = Statut.EN_ATTENTE;

    @Column(name = "reference_externe")
    private String referenceExterne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (statut == null) statut = Statut.EN_ATTENTE;
    }

    public enum Type { CREDIT, DEBIT }
    public enum Categorie { DEPOT, RETRAIT, MISE, GAIN_RESOLUTION, REMBOURSEMENT, AJUSTEMENT_ADMIN }
    public enum Statut { EN_ATTENTE, CONFIRME, ECHOUE, ANNULE }
}