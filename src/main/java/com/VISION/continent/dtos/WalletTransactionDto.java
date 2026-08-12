package com.VISION.continent.dtos;

import com.VISION.continent.entity.WalletTransaction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class WalletTransactionDto {

    @Getter @Setter @Builder
    @Schema(name = "WalletTransactionResponse")
    public static class Response {
        private UUID id;
        private WalletTransaction.Type type;
        private WalletTransaction.Categorie categorie;
        private BigDecimal montantFcfa;
        private BigDecimal soldeAvant;
        private BigDecimal soldeApres;
        private WalletTransaction.Statut statut;
        private String referenceExterne;
        private String description;
        private LocalDateTime createdAt;
    }
}