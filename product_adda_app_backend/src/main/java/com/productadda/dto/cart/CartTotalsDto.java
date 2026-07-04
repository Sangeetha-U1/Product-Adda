package com.productadda.dto.cart;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartTotalsDto {

    private int itemCount;
    private BigDecimal subtotal;
    private BigDecimal couponDiscount;
    private BigDecimal shippingCost;
    private BigDecimal taxAmount;
    private BigDecimal total;
}