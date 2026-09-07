package com.VISION.continent.dtos;

import com.VISION.continent.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserDto {

    @Getter
    @Setter
    @Builder
    public static class Response {
        private UUID id;
        private String nom;
        private String prenom;
        private String telephone;
        private String email;
        private User.Role role;
        private String villeCameroun;
        private BigDecimal soldeFcfa;
        private User.Statut statut;
        private LocalDateTime createdAt;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String nom;
        private String prenom;
        private String email;
        private String villeCameroun;
    }
}
