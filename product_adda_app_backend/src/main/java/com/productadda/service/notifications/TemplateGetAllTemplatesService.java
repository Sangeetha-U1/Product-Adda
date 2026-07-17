package com.productadda.service.notifications;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.notifications.NotificationTemplateDto;

import com.productadda.entity.NotificationTemplate;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.NotificationTemplateRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateGetAllTemplatesService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;

    /*
     * ================================================================
     * GET ALL TEMPLATES
     * Description: Admin-only. Returns every notification_templates
     * row, optionally filtered by eventType and/or channel name.
     * Unpaginated -- dataset is small (baseline: ~18 role-
     * agnostic rows), per Dheeraj's decision.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public List<NotificationTemplateDto> getAllTemplates(String eventType, String channel) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        String safeEventType = (eventType == null || eventType.trim().isEmpty())
                ? null
                : eventType.trim().toUpperCase();
        String safeChannel = (channel == null || channel.trim().isEmpty())
                ? null
                : channel.trim().toUpperCase();

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

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        List<NotificationTemplate> templates = notificationTemplateRepository
                .findAllByOrderByFkType_NotificationTypeNameAscFkChannel_ChannelNameAsc();

        List<NotificationTemplate> filteredTemplates = templates.stream()
                .filter(template -> safeEventType == null
                        || safeEventType.equals(template.getFkType().getNotificationTypeName()))
                .filter(template -> safeChannel == null
                        || safeChannel.equals(template.getFkChannel().getChannelName()))
                .toList();

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * Reason: Read-only lookup, no mutations performed.
         * ================================================================
         */

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
        return filteredTemplates.stream()
                .map(template -> NotificationTemplateDto.builder()
                        .templateId(template.getPkTemplateId())
                        .eventType(template.getFkType().getNotificationTypeName())
                        .channel(template.getFkChannel().getChannelName())
                        .recipientRole(template.getFkRecipientRole() != null
                                ? template.getFkRecipientRole().getRoleName()
                                : null)
                        .subject(template.getSubject())
                        .body(template.getBody())
                        .createdByUserId(template.getFkCreatedByUser() != null
                                ? template.getFkCreatedByUser().getPkUserId()
                                : null)
                        .updatedByUserId(template.getFkUpdatedByUser() != null
                                ? template.getFkUpdatedByUser().getPkUserId()
                                : null)
                        .createdAtUtc(template.getCreatedAtUtc())
                        .updatedAtUtc(template.getUpdatedAtUtc())
                        .build())
                .collect(Collectors.toList());
    }
}
