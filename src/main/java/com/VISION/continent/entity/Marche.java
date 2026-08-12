package com.VISION.continent.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "marches")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Marche {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id", nullable = false)
    private Evenement evenement;

    @Column(nullable = false)
    private String question;

    @Column(name = "icone_url")
    private String iconeUrl;

    // Infos spécifiques à cette option (palmarès, forme, biographie...) pour
    // guider l'utilisateur dans son choix.
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "pool_oui_fcfa", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal poolOuiFcfa = BigDecimal.ZERO;

    @Column(name = "pool_non_fcfa", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal poolNonFcfa = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.OUVERT;

    @Column(name = "outcome_gagnant")
    private String outcomeGagnant;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @OneToMany(mappedBy = "marche", cascade = CascadeType.ALL)
    private List<Position> positions;

    @Transient
    public BigDecimal getPrixOui() {
        BigDecimal total = poolOuiFcfa.add(poolNonFcfa);
        if (total.compareTo(BigDecimal.ZERO) == 0) return new BigDecimal("0.50");
        return poolOuiFcfa.divide(total, 4, RoundingMode.HALF_UP);
    }

    @Transient
    public BigDecimal getPrixNon() {
        return BigDecimal.ONE.subtract(getPrixOui());
    }

    @Transient
    public BigDecimal getVolumeFcfa() {
        return poolOuiFcfa.add(poolNonFcfa);
    }

    public enum Statut { OUVERT, EN_RESOLUTION, RESOLU, ANNULE }
}