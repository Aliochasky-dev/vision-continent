package com.VISION.continent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String identifier; // ici : le téléphone

    @Builder.Default
    private int nbEchecs = 0;

    private LocalDateTime dernierEchec;

    private LocalDateTime bloqueJusquA;

    private String ipAddress;
}