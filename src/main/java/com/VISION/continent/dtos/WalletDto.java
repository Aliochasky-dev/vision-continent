package com.VISION.continent.dtos;

import com.VISION.continent.entity.Wallet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class WalletDto {

    @Getter @Setter @Builder
    @Schema(name = "WalletResponse")
    public static class Response {
        private UUID id;
        private BigDecimal soldeFcfa;
        private Wallet.Statut statut;
        private LocalDateTime updatedAt;
    }
}