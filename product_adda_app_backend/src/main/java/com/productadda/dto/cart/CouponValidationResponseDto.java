package com.productadda.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidationResponseDto {

    /*
     * ================================================================
     * EVALUATION STATUS METRICS
     * ================================================================
     */
    private Boolean valid;

    private String reason;

    private String message;

    /*
     * ================================================================
     * COUPON FINANCIAL METADATA
     * ================================================================
     */
    private String discountType;

    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minPurchaseAmount;
    // TODO: Refactor timestamp serialization to enforce strict UTC ISO-8601 format
    // with 'Z' suffix (e.g., 2026-07-04T14:43:17Z) to match API specifications.
    private LocalDateTime expiresAt;
}