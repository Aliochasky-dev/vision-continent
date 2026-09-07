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

    /** Renvoie le premier champ non vide entre `reference` et `numero`. */
    private static String contact(String reference, String numero) {
        if (reference != null && !reference.isBlank()) return reference;
        return numero;
    }

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

        // Le numéro/référence Mobile Money — les deux noms sont acceptés pour
        // compatibilité (voir getContact()).
        @Schema(example = "+237699000001", description = "Numéro Mobile Money")
        private String numeroCible;

        @Schema(example = "OM20260522001", description = "Référence Mobile Money (alias de numeroCible)")
        private String referenceMobileMoney;

        public String getContact() { return contact(referenceMobileMoney, numeroCible); }
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

        @Schema(example = "+237699000001", description = "Numéro Mobile Money de destination")
        private String numeroCible;

        @Schema(example = "OM20260522001", description = "Référence Mobile Money (alias de numeroCible)")
        private String referenceMobileMoney;

        public String getContact() { return contact(referenceMobileMoney, numeroCible); }
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