package com.VISION.continent.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "evenements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_id", nullable = false)
    private User createur;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String contexte;

    @Column(name = "regles_resolution", columnDefinition = "TEXT")
    private String reglesResolution;

    @Column(name = "source_resolution")
    private String sourceResolution;

    @Column(name = "icone_url")
    private String iconeUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_choix", nullable = false)
    private TypeChoix typeChoix = TypeChoix.UNIQUE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.OUVERT;

    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;

    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;

    @Column(name = "nb_traders")
    private Integer nbTraders = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL)
    private List<Marche> marches;

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL)
    private List<Commentaire> commentaires;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (nbTraders == null) nbTraders = 0;
        if (statut == null) statut = Statut.OUVERT;
        if (typeChoix == null) typeChoix = TypeChoix.UNIQUE;
    }

    // Volume total = somme des volumes de toutes les Marches (calculé, plus stocké en dur)
    @Transient
    public BigDecimal getVolumeTotalFcfa() {
        if (marches == null) return BigDecimal.ZERO;
        return marches.stream()
                .map(Marche::getVolumeFcfa)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public enum Statut { OUVERT, EN_RESOLUTION, RESOLU, ANNULE }
    public enum TypeChoix { UNIQUE, MULTIPLE }
}