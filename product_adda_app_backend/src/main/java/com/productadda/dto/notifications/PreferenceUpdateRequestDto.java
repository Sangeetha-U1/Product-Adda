package com.productadda.dto.notifications;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenceUpdateRequestDto {

    private Boolean notificationsEnabled;

    private Boolean emailEnabled;

    private Boolean smsEnabled;

    private Boolean pushEnabled;

    private Boolean inAppEnabled;

    private String quietHoursStart;

    private String quietHoursEnd;

    private List<String> eventOptOuts;
}
