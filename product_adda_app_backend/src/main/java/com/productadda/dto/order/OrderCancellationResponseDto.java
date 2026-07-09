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
 * Description: Sanitized confirmation payload returned by both the
 * customer self-cancel and admin force-cancel endpoints.
 * previousStatusName is only populated on the admin force-cancel path
 * (null on the customer path, since customers can only cancel from a
 * fixed PENDING/CONFIRMED starting point, making it less relevant
 * there, though the field is always present in the response shape).
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancellationResponseDto {

    private UUID orderId;

    private String statusName;

    private String cancelledBy;

    private LocalDateTime cancelledAtUtc;

    private boolean inventoryReleased;

    private String previousStatusName;

    private String cancellationReason;
}
