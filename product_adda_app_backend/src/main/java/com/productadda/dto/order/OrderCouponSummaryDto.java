package com.productadda.dto.order;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * NESTED ITEM DTO
 * Description: Read-only coupon snapshot embedded inside order
 * detail responses. Only populated when the order used a coupon.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCouponSummaryDto {

    private UUID couponId;

    private String couponCode;

    private String discountType;

    private BigDecimal discountValue;
}
