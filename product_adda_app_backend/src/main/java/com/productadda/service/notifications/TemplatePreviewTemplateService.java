package com.productadda.service.notifications;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.notifications.TemplatePreviewRequestDto;
import com.productadda.dto.notifications.TemplatePreviewResponseDto;

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
public class TemplatePreviewTemplateService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final TemplateRenderingEngine templateRenderingEngine;

    /*
     * ================================================================
     * PREVIEW TEMPLATE
     * Description: Admin-only. Renders a template's subject/body
     * against admin-supplied sample context, without persisting
     * anything -- lets an admin see what a real notification would
     * look like before saving a template edit.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public TemplatePreviewResponseDto previewTemplate(UUID templateId, TemplatePreviewRequestDto requestDto) {

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

        Map<String, Object> sampleContext = (requestDto == null || requestDto.getSampleContext() == null)
                ? Map.of()
                : requestDto.getSampleContext();

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

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        String renderedSubject = templateRenderingEngine.render(template.getSubject(), sampleContext);
        String renderedBody = templateRenderingEngine.render(template.getBody(), sampleContext);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Reason: N/A - preview only, nothing persisted.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: N/A - see section 3.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return TemplatePreviewResponseDto.builder()
                .templateId(template.getPkTemplateId())
                .renderedSubject(renderedSubject)
                .renderedBody(renderedBody)
                .build();
    }
}
