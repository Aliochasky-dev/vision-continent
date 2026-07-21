package com.VISION.continent.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ← ManyToOne vers User dont l'id est Long (bigint en BDD)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_tx", nullable = false)
    private TypeTransaction typeTx;

    @Column(name = "montant_fcfa", precision = 15, scale = 2, nullable = false)
    private BigDecimal montantFcfa;

    @Column(name = "reference_mobile_money")
    private String referenceMobileMoney;

    @Enumerated(EnumType.STRING)
    private Operateur operateur;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_paiement", nullable = false)
    private StatutPaiement statutPaiement = StatutPaiement.EN_ATTENTE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TypeTransaction { DEPOT, RETRAIT, ACHAT_PARTS, GAIN_RESOLUTION, REMBOURSEMENT }
    public enum Operateur { ORANGE_MONEY, MTN_MOMO }
    public enum StatutPaiement { EN_ATTENTE, CONFIRME, ECHOUE, REMBOURSE }
}