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
 * returned after assigning a delivery partner to an order.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAssignmentResponseDto {

    private UUID assignmentId;

    private UUID orderId;

    private UUID partnerId;

    private String partnerName;

    private String assignmentStatusName;

    private LocalDateTime assignedAt;
}
