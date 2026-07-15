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
public class NotificationListItemDto {

    private UUID notificationId;

    private String notificationTypeName;

    private String title;

    private String message;

    private Boolean isRead;

    private String dispatchStatusName;

    private LocalDateTime sentAtUtc;

    private LocalDateTime readAtUtc;

    private LocalDateTime createdAtUtc;
}
