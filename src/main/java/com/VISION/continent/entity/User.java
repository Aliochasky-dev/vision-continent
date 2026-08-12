package com.VISION.continent.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String telephone;

    @Column(unique = true)
    private String email;

    // ← "password" correspond exactement à la colonne en BDD
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "email_verifie", nullable = false)
    @Builder.Default
    private boolean emailVerifie = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(name = "ville_cameroun")
    private String villeCameroun;

    @Column(name = "solde_fcfa", precision = 15, scale = 2)
    private BigDecimal soldeFcfa = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.ACTIF;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (soldeFcfa == null) soldeFcfa = BigDecimal.ZERO;
        if (role == null) role = Role.USER;
        if (statut == null) statut = Statut.ACTIF;
    }

    public enum Role { USER, ADMIN, MODERATEUR }
    public enum Statut { ACTIF, SUSPENDU, BANNI }
}