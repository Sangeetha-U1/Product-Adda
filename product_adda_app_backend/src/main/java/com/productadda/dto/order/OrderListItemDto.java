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
 * NESTED ITEM DTO
 * Description: Minimal, list-view order summary. Deliberately
 * excludes line items to keep paginated list payloads lightweight.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListItemDto {

    private UUID orderId;

    private String orderNumber;

    private String statusName;

    private BigDecimal totalAmount;

    private LocalDateTime createdAtUtc;
}
