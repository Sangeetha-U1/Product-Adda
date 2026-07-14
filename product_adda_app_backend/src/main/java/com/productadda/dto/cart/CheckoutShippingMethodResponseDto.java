package com.productadda.dto.cart;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutShippingMethodResponseDto {

    private UUID shippingMethodId;
    private String shippingMethodName;
    private String description;
    private BigDecimal baseCost;
    private BigDecimal costPerKg;
    private Integer estimatedDeliveryDays;
}