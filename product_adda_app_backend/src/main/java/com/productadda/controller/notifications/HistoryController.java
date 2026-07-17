package com.productadda.controller.notifications;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.notifications.NotificationHistoryResponseDto;

import com.productadda.service.notifications.HistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    /*
     * ================================================================
     * GET HISTORY
     * GET /api/notifications/history
     * Auth Required - any authenticated role. Own notifications only,
     * across all channels. All filters optional; no default date range.
     * ================================================================
     */
    @GetMapping("/history")
    public ResponseEntity<ApiSuccessResponseDto<NotificationHistoryResponseDto>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {

        NotificationHistoryResponseDto response = historyService.getHistory(
                page, pageSize, eventType, channel, dateFrom, dateTo);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<NotificationHistoryResponseDto>builder()
                        .success(true)
                        .message("Notification history retrieved successfully")
                        .data(response)
                        .build());
    }
}
