package com.productadda.dto.notifications;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateDto {

    private UUID templateId;

    private String eventType;

    private String channel;

    // null = applies to all recipient roles
    private String recipientRole;

    private String subject;

    private String body;

    private UUID createdByUserId;

    private UUID updatedByUserId;

    private LocalDateTime createdAtUtc;

    private LocalDateTime updatedAtUtc;
}
