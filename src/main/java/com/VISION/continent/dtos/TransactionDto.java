package com.VISION.continent.dtos;

import com.VISION.continent.entity.Transaction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionDto {

    @Getter @Setter
    @Schema(name = "DepotRequest", description = "Initier un dépôt Mobile Money")
    public static class DepotRequest {
        @NotNull
        @Positive
        @Min(value = 500, message = "Dépôt minimum : 500 FCFA")
        @Schema(example = "5000", description = "Montant à déposer en FCFA (minimum 500)")
        private BigDecimal montantFcfa;

        @NotNull
        @Schema(example = "ORANGE_MONEY", description = "ORANGE_MONEY ou MTN_MOMO")
        private Transaction.Operateur operateur;

        @NotBlank
        @Schema(example = "OM20260522001", description = "Référence de la transaction Mobile Money")
        private String referenceMobileMoney;
    }

    @Getter @Setter
    @Schema(name = "RetraitRequest", description = "Initier un retrait vers Mobile Money")
    public static class RetraitRequest {
        @NotNull
        @Positive
        @Min(value = 1000, message = "Retrait minimum : 1000 FCFA")
        @Schema(example = "2000", description = "Montant à retirer en FCFA (minimum 1000)")
        private BigDecimal montantFcfa;

        @NotNull
        @Schema(example = "MTN_MOMO", description = "ORANGE_MONEY ou MTN_MOMO")
        private Transaction.Operateur operateur;

        @NotBlank
        @Schema(example = "+237699000001", description = "Numéro Mobile Money de destination")
        private String numeroCible;
    }

    @Getter @Setter @Builder
    @Schema(name = "TransactionResponse")
    public static class Response {
        private UUID id;
        private Transaction.TypeTransaction typeTx;
        private BigDecimal montantFcfa;
        private Transaction.Operateur operateur;
        private String referenceMobileMoney;
        private Transaction.StatutPaiement statutPaiement;
        private LocalDateTime createdAt;
    }
}