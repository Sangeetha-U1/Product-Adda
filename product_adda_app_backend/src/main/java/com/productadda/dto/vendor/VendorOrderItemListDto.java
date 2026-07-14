package com.productadda.dto.vendor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * RESPONSE DTO
 * Description: Sanitized, vendor-scoped view of an order containing
 * only this vendor's items. Customer identity is masked to first
 * name only, per Day 1 vendor-visibility requirements.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorOrderItemListDto {

    private UUID orderId;

    private String orderNumber;

    private String orderStatusName;

    private String customerFirstName;

    private BigDecimal orderTotalAmount;

    private LocalDateTime createdAtUtc;

    private List<VendorOrderItemDetailDto> items;
}
