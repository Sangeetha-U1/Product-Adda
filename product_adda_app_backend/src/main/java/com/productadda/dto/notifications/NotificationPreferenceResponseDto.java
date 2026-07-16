package com.productadda.dto.notifications;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponseDto {

    private UUID preferenceId;

    private Boolean notificationsEnabled;

    private Boolean emailEnabled;

    private Boolean smsEnabled;

    private Boolean pushEnabled;

    private Boolean inAppEnabled;

    private String quietHoursStart;

    private String quietHoursEnd;

    private List<String> eventOptOuts;

    private LocalDateTime updatedAtUtc;
}
