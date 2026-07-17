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
public class NotificationHistoryDto {

    private UUID notificationId;

    private String eventType;

    private String channel;

    private String deliveryStatus;

    // No separate "delivered_at" -- this system has no real delivery
    // confirmation (no SMTP bounce/receipt handling exists anywhere in
    // this project). sentAtUtc is the closest available signal: the
    // moment dispatch to the mail server succeeded.
    private LocalDateTime sentAtUtc;

    private String errorMessage;

    private LocalDateTime createdAtUtc;
}
