package com.productadda.service.notifications;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.notifications.NotificationTemplateDto;
import com.productadda.dto.notifications.TemplateUpdateRequestDto;

import com.productadda.entity.AuditActionType;
import com.productadda.entity.NotificationAuditLog;
import com.productadda.entity.NotificationTemplate;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.AuditActionTypeRepository;
import com.productadda.repository.NotificationAuditLogRepository;
import com.productadda.repository.NotificationTemplateRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateUpdateTemplateService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final TemplateSyntaxValidatorService templateSyntaxValidatorService;
    private final AuditActionTypeRepository auditActionTypeRepository;
    private final NotificationAuditLogRepository notificationAuditLogRepository;
    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * UPDATE TEMPLATE
     * Description: Admin-only. Updates subject/body on an existing
     * template (the composite key -- event type, channel, recipient
     * role -- is not editable; only content is). Logs the change to
     * notification_audit_logs with the acting admin's user id, unlike
     * the self-service preference update which logs admin_id = null.
     * ================================================================
     */
    @Transactional
    public NotificationTemplateDto updateTemplate(UUID templateId, TemplateUpdateRequestDto requestDto) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (templateId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "templateId is required");
        }
        if (requestDto == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }

        templateSyntaxValidatorService.validate(requestDto.getSubject(), "subject");
        templateSyntaxValidatorService.validate(requestDto.getBody(), "body");

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
        }

        String currentUsername = authentication.getName();

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);

        boolean hasAdminRole = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .anyMatch(roleName -> "ADMIN".equals(roleName) || "SUPER_ADMIN".equals(roleName));

        if (!hasAdminRole) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required to access this endpoint");
        }

        NotificationTemplate template = notificationTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Template not found"));

        AuditActionType templateUpdatedActionType = auditActionTypeRepository
                .findByActionTypeName("TEMPLATE_UPDATED")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "TEMPLATE_UPDATED audit action type lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("subject", template.getSubject());
        oldValue.put("body", template.getBody());

        template.setSubject(requestDto.getSubject());
        template.setBody(requestDto.getBody());
        template.setFkUpdatedByUser(currentUser);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        NotificationTemplate savedTemplate = notificationTemplateRepository.save(template);

        NotificationTemplate reloadedTemplate = notificationTemplateRepository
                .findById(savedTemplate.getPkTemplateId())
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Template persisted but could not be reloaded"));

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("subject", reloadedTemplate.getSubject());
        newValue.put("body", reloadedTemplate.getBody());

        NotificationAuditLog auditLog = NotificationAuditLog.builder()
                .pkAuditLogId(uuidUtil.generateUuidV7())
                .fkAdminUser(currentUser)
                .fkAuditActionType(templateUpdatedActionType)
                .targetTable("notification_templates")
                .targetId(reloadedTemplate.getPkTemplateId())
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

        notificationAuditLogRepository.save(auditLog);

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: No sensitive fields to strip.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return NotificationTemplateDto.builder()
                .templateId(reloadedTemplate.getPkTemplateId())
                .eventType(reloadedTemplate.getFkType().getNotificationTypeName())
                .channel(reloadedTemplate.getFkChannel().getChannelName())
                .recipientRole(reloadedTemplate.getFkRecipientRole() != null
                        ? reloadedTemplate.getFkRecipientRole().getRoleName()
                        : null)
                .subject(reloadedTemplate.getSubject())
                .body(reloadedTemplate.getBody())
                .createdByUserId(reloadedTemplate.getFkCreatedByUser() != null
                        ? reloadedTemplate.getFkCreatedByUser().getPkUserId()
                        : null)
                .updatedByUserId(reloadedTemplate.getFkUpdatedByUser() != null
                        ? reloadedTemplate.getFkUpdatedByUser().getPkUserId()
                        : null)
                .createdAtUtc(reloadedTemplate.getCreatedAtUtc())
                .updatedAtUtc(reloadedTemplate.getUpdatedAtUtc())
                .build();
    }
}
