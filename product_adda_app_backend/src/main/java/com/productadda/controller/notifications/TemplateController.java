package com.productadda.controller.notifications;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.notifications.NotificationTemplateDto;
import com.productadda.dto.notifications.TemplatePreviewRequestDto;
import com.productadda.dto.notifications.TemplatePreviewResponseDto;
import com.productadda.dto.notifications.TemplateUpdateRequestDto;

import com.productadda.service.notifications.TemplateGetAllTemplatesService;
import com.productadda.service.notifications.TemplatePreviewTemplateService;
import com.productadda.service.notifications.TemplateUpdateTemplateService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateGetAllTemplatesService templateGetAllTemplatesService;
    private final TemplateUpdateTemplateService templateUpdateTemplateService;
    private final TemplatePreviewTemplateService templatePreviewTemplateService;

    /*
     * ================================================================
     * GET ALL TEMPLATES
     * GET /api/notifications/templates
     * Auth Required - Admin role. Unpaginated, optional eventType/
     * channel filters.
     * ================================================================
     */
    @GetMapping
    public ResponseEntity<ApiSuccessResponseDto<List<NotificationTemplateDto>>> getAllTemplates(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String channel) {

        List<NotificationTemplateDto> response = templateGetAllTemplatesService.getAllTemplates(eventType, channel);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<List<NotificationTemplateDto>>builder()
                        .success(true)
                        .message("Notification templates retrieved successfully")
                        .data(response)
                        .build());
    }

    /*
     * ================================================================
     * UPDATE TEMPLATE
     * PATCH /api/notifications/templates/{templateId}
     * Auth Required - Admin role. Subject/body only -- event type,
     * channel, and recipient role are not editable.
     * ================================================================
     */
    @PatchMapping("/{templateId}")
    public ResponseEntity<ApiSuccessResponseDto<NotificationTemplateDto>> updateTemplate(
            @PathVariable UUID templateId,
            @RequestBody TemplateUpdateRequestDto requestDto) {

        NotificationTemplateDto response = templateUpdateTemplateService.updateTemplate(templateId, requestDto);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<NotificationTemplateDto>builder()
                        .success(true)
                        .message("Notification template updated successfully")
                        .data(response)
                        .build());
    }

    /*
     * ================================================================
     * PREVIEW TEMPLATE
     * POST /api/notifications/templates/{templateId}/preview
     * Auth Required - Admin role. Renders against sample context;
     * nothing is persisted.
     * ================================================================
     */
    @PostMapping("/{templateId}/preview")
    public ResponseEntity<ApiSuccessResponseDto<TemplatePreviewResponseDto>> previewTemplate(
            @PathVariable UUID templateId,
            @RequestBody TemplatePreviewRequestDto requestDto) {

        TemplatePreviewResponseDto response = templatePreviewTemplateService.previewTemplate(templateId, requestDto);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<TemplatePreviewResponseDto>builder()
                        .success(true)
                        .message("Template preview rendered successfully")
                        .data(response)
                        .build());
    }
}
