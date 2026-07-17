package com.productadda.dto.notifications;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistoryResponseDto {

    private List<NotificationHistoryDto> notifications;

    private long totalCount;

    private int page;

    private int pageSize;
}
