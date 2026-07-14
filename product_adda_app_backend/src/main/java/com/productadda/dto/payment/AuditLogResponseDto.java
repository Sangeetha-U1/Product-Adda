package com.productadda.dto.payment;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDto {

    private String id;

    private String paymentId;

    private String refundId;

    private String action;

    private String actorId;

    private String actorName;

    private String actorRole;

    private String oldStatus;

    private String newStatus;

    // Sanitized JSON string - see ComplianceAuditLogService for masking rules
    private Map<String, Object> metadata;

    private LocalDateTime createdAtUtc;
}
