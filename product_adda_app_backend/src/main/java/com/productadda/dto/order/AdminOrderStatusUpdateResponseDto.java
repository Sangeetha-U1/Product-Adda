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
 * Description: Sanitized confirmation payload returned by the admin
 * force status transition endpoint, including audit-relevant fields
 * (previous status, reason, and a non-blocking warning string for
 * soft-allowed edge cases like transitioning to DELIVERED while
 * items are still PROCESSING).
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderStatusUpdateResponseDto {

    private UUID orderId;

    private String previousStatusName;

    private String newStatusName;

    private String reason;

    private LocalDateTime updatedAtUtc;

    private boolean deliveryPartnerActiveDeliveriesIncremented;

    private String warning;
}
