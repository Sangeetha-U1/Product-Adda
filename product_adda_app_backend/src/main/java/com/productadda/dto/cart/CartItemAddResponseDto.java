package com.productadda.dto.cart;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemAddResponseDto {

    private UUID itemId;
    private UUID cartId;
    private UUID productId;
    private int quantity;
    private BigDecimal priceAtAdd;
    private boolean priceAlert;
}