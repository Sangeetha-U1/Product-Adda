package com.productadda.dto.cart;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDto {
    // TODO: Refactor timestamp serialization to enforce strict UTC ISO-8601 format
    // with 'Z' suffix (e.g., 2026-07-04T14:43:17Z) to match API specifications.
    private UUID cartId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private List<CartItemResponseDto> items;
    private CartTotalsDto totals;
    private LocalDateTime lastUpdated;
}