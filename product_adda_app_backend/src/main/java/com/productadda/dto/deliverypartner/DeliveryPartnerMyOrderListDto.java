package com.productadda.dto.deliverypartner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * NESTED ITEM DTO
 * Description: Sanitized summary of a single order currently
 * assigned to the authenticated delivery partner. Used for both
 * the active "my orders" view (Phase 2) and the delivery history
 * view (Phase 5), since the underlying shape is identical -- the
 * history view will simply have all three timestamp fields set.
 * ================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerMyOrderListDto {

    private UUID orderId;
    private String orderNumber;
    private String statusName;
    private LocalDateTime createdAtUtc;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private Integer estimatedPackageCount;
    private String itemSummary;
    private String city;
    private String state;
    private String postalCode;
    private LocalDateTime pickedUpAtUtc;
    private LocalDateTime outForDeliveryAtUtc;
    private LocalDateTime deliveredAtUtc;
}
