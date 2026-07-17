package com.productadda.dto.notifications;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHealthReportResponseDto {

    // Keyed by channel name (EMAIL, SMS, PUSH, IN_APP) -- not by vendor
    // product name (SendGrid/Twilio/FCM), since this project uses plain
    // SMTP rather than a named provider API. See CHANGES_DAY4.md.
    private Map<String, ChannelHealthDto> channels;
}
