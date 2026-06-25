package com.productadda.dto.vendor;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDashboardMetricsResponseDto {

    /*
     * ================================================================
     * VENDOR IDENTITY
     * ================================================================
     */
    private UUID vendorId;

    /*
     * ================================================================
     * PRODUCT METRICS
     * ================================================================
     */
    private long totalProducts;

    /*
     * ================================================================
     * ORDER METRICS
     * ================================================================
     */
    private long totalOrderItems;
    private long totalDistinctOrders;

    /*
     * ================================================================
     * VENDOR STATUS
     * ================================================================
     */
    private Boolean isActive;
}