package com.productadda.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.payment.PaginatedAuditLogResponseDto;

import com.productadda.service.payment.ComplianceAuditLogService;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * ComplianceController
 * Admin-only.
 * ================================================================
 */
@RestController
@RequiredArgsConstructor
public class AdminComplianceController {

        private final ComplianceAuditLogService complianceAuditLogService;

        @GetMapping("/api/admin/compliance/audit-logs")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<PaginatedAuditLogResponseDto>> getAuditLogs(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "50") int size,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        @RequestParam(required = false) String action,
                        @RequestParam(required = false) String actorRole,
                        @RequestParam(required = false) String paymentId) {

                PaginatedAuditLogResponseDto response = complianceAuditLogService
                                .getAuditLogs(page, size, startDate, endDate, action, actorRole, paymentId);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<PaginatedAuditLogResponseDto>builder()
                                                .success(true)
                                                .message("Audit logs retrieved successfully")
                                                .data(response)
                                                .build());
        }
}
