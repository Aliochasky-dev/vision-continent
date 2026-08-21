package com.VISION.continent.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

public class NokashDto {

    // ─── Requête envoyée à Nokash ─────────────────────────────
    @Getter @Setter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class PayinRequest {
        @JsonProperty("i_space_key")
        private String iSpaceKey;

        @JsonProperty("app_space_key")
        private String appSpaceKey;

        @JsonProperty("payment_type")
        private String paymentType;

        private String country;

        @JsonProperty("payment_method")
        private String paymentMethod;

        @JsonProperty("order_id")
        private String orderId;

        private String amount; // ← changé de BigDecimal à String

        @JsonProperty("callback_url")
        private String callbackUrl;

        @JsonProperty("user_data")
        private UserData userData;

        @Getter @Setter @Builder
        @NoArgsConstructor @AllArgsConstructor
        public static class UserData {
            @JsonProperty("user_phone")
            private String userPhone;
        }
    }

    // ─── Réponse reçue à l'initiation ─────────────────────────
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class PayinResponse {
        private String status;   // "REQUEST_OK"
        private String message;
        private Data data;
        private String code;

        @Getter @Setter
        @NoArgsConstructor @AllArgsConstructor
        public static class Data {
            private String id;       // identifiant Nokash (nk_tx_...)
            private String status;   // PENDING / SUCCESS / FAILED
            private String amount;
            private String orderId;
            private String phone;
            private String initiatedAt;
            private String statusReason; // présent en cas d'échec
        }
    }

    // ─── Body reçu sur le webhook ──────────────────────────────
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(name = "NokashCallback")
    public static class Callback {
        private String id;
        private String status;
        private BigDecimal amount;
        private String phone;
        private String orderId;
    }
    // ─── Auth (génération de la clé de retrait, usage unique, expire 2 min) ──
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class AuthResponse {
        private String status;   // LOGIN_SUCCESS / LOGIN_BAD_INFOS
        private String message;
        private String data;     // la clé générée, à mettre dans le header auth-code
    }

    // ─── Requête de payout ─────────────────────────────────────────
    @Getter @Setter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class PayoutRequest {
        @JsonProperty("i_space_key")
        private String iSpaceKey;

        @JsonProperty("app_space_key")
        private String appSpaceKey;

        @JsonProperty("payment_type")
        private String paymentType;

        private String country;

        @JsonProperty("payment_method")
        private String paymentMethod;

        @JsonProperty("order_id")
        private String orderId;

        private String amount;

        @JsonProperty("callback_url")
        private String callbackUrl;

        @JsonProperty("user_data")
        private PayinRequest.UserData userData; // même structure que le payin (user_phone)
    }

    // ─── Réponse de payout (même structure que PayinResponse) ─────────
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class PayoutResponse {
        private String status;
        private String message;
        private Data data;

        @Getter @Setter
        @NoArgsConstructor @AllArgsConstructor
        public static class Data {
            private String id;
            private String status;    // PENDING, FAILED, CANCELED, TIMEOUT, SUCCESS
            private String amount;
            private String orderId;
            private String phone;
            private String statusReason;
        }
    }
}