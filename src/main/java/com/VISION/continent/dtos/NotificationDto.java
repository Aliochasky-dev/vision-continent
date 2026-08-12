package com.VISION.continent.dtos;

import com.VISION.continent.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationDto {

    @Getter @Setter @Builder
    @Schema(name = "NotificationResponse")
    public static class Response {
        private UUID id;
        private Notification.TypeNotif typeNotif;
        private String titre;
        private String message;
        private boolean lue;
        private LocalDateTime createdAt;
    }
}
