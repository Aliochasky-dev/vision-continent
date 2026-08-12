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

public class RetraitDto {

    @Getter @Setter
    @Schema(name = "WalletRetraitRequest", description = "Initier un retrait via CinetPay")
    public static class Request {
        @NotNull
        @Positive
        @Min(value = 1000, message = "Retrait minimum : 1000 FCFA")
        @Schema(example = "2000", description = "Montant à retirer en FCFA (minimum 1000)")
        private BigDecimal montantFcfa;

        @NotBlank
        @Schema(example = "+237690123456", description = "Numéro Mobile Money de destination")
        private String telephoneDestination;
    }

    @Getter @Setter @Builder
    @Schema(name = "WalletRetraitResponse")
    public static class Response {
        private UUID walletTransactionId;
        private UUID paymentIntentId;
        private BigDecimal montantFcfa;
        private String statut;
    }
}