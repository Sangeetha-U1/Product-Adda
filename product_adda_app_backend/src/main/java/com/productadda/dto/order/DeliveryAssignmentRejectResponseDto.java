package com.productadda.dto.order;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * RESPONSE DTO
 * Description: Sanitized, frontend-ready confirmation payload
 * returned after a delivery partner rejects an assignment. Kept as a
 * separate DTO from DeliveryAssignmentResponseDto (used by accept and
 * by Day 4's assignment creation) rather than overloading one shape
 * with fields that are only relevant to one outcome, per your
 * explicit instruction.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAssignmentRejectResponseDto {

    private UUID assignmentId;

    private UUID orderId;

    private UUID partnerId;

    private String assignmentStatusName;

    private LocalDateTime rejectedAt;

    private String rejectionReason;
}
