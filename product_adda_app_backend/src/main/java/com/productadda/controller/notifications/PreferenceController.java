package com.productadda.controller.notifications;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.notifications.NotificationPreferenceResponseDto;
import com.productadda.dto.notifications.PreferenceUpdateRequestDto;

import com.productadda.service.notifications.PreferenceGetPreferencesService;
import com.productadda.service.notifications.PreferenceUpdatePreferencesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceGetPreferencesService preferenceGetPreferencesService;
    private final PreferenceUpdatePreferencesService preferenceUpdatePreferencesService;

    /*
     * ================================================================
     * GET PREFERENCES
     * GET /api/notifications/preferences
     * Auth Required - own preferences only. Lazily creates a default
     * row on first access if none exists.
     * ================================================================
     */
    @GetMapping
    public ResponseEntity<ApiSuccessResponseDto<NotificationPreferenceResponseDto>> getPreferences() {

        NotificationPreferenceResponseDto response = preferenceGetPreferencesService.getPreferences();

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<NotificationPreferenceResponseDto>builder()
                        .success(true)
                        .message("Notification preferences retrieved successfully")
                        .data(response)
                        .build());
    }

    /*
     * ================================================================
     * UPDATE PREFERENCES
     * PATCH /api/notifications/preferences
     * Auth Required - own preferences only. Full replacement update
     * (every field required), not a partial merge.
     * ================================================================
     */
    @PatchMapping
    public ResponseEntity<ApiSuccessResponseDto<NotificationPreferenceResponseDto>> updatePreferences(
            @RequestBody PreferenceUpdateRequestDto requestDto) {

        NotificationPreferenceResponseDto response = preferenceUpdatePreferencesService
                .updatePreferences(requestDto);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<NotificationPreferenceResponseDto>builder()
                        .success(true)
                        .message("Notification preferences updated successfully")
                        .data(response)
                        .build());
    }
}
