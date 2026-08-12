package com.VISION.continent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "payment_providers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mode mode;

    public enum Mode { SANDBOX, PRODUCTION }
}