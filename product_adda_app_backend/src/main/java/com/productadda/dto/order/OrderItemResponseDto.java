package com.productadda.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * RESPONSE DTO
 * Description: Sanitized, frontend-ready representation of a single
 * order item after a vendor status transition. Reloaded from the
 * database after save so updatedAtUtc reflects the DB-trigger value,
 * per the Unified Temporal Alignment standard.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDto {

    private UUID orderItemId;

    private UUID orderId;

    private UUID productId;

    private String productNameSnapshot;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal lineTotal;

    private String itemStatusName;

    private LocalDateTime updatedAtUtc;
}
