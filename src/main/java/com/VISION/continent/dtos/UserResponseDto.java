package com.VISION.continent.dtos;

import com.VISION.continent.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserResponseDto {

    //private Long id;
   // private String nom;
    private String email;
    private UUID id;
    private String nom;
    private String prenom;
    private String telephone;
    private User.Role role;
    private String villeCameroun;
    private BigDecimal soldeFcfa;
    private User.Statut statut;
    private LocalDateTime createdAt;

    public UserResponseDto(UUID id, String nom, String email) {
        this.id = id;
        this.nom = nom;
        this.email = email;
    }

    // Getters
    public Long getId() { return getId(); }
    public String getName() { return nom; }
    public String getEmail() { return email; }
}