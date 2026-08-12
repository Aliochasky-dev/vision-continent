package com.VISION.continent.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

public class DepotDto {

    @Getter @Setter
    @Schema(name = "WalletDepotRequest", description = "Initier un dépôt via CinetPay")
    public static class Request {
        @NotNull
        @Positive
        @Min(value = 500, message = "Dépôt minimum : 500 FCFA")
        @Schema(example = "5000", description = "Montant à déposer en FCFA (minimum 500)")
        private BigDecimal montantFcfa;

        @NotBlank
        @Schema(example = "+237690123456", description = "Numéro de téléphone pour le paiement Mobile Money")
        private String telephonePaiement;

        @Schema(example = "Rechargement de compte VISION", description = "Description affichée sur la page de paiement")
        private String description;
    }

    @Getter @Setter @Builder
    @Schema(name = "WalletDepotResponse")
    public static class Response {
        private UUID walletTransactionId;
        private UUID paymentIntentId;
        private String paymentUrl;
        private BigDecimal montantFcfa;
        private String statut;
    }
}