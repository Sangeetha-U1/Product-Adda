package com.productadda.dto.cart;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * NESTED ITEM DTO
 * Description: Represents a single non-fatal warning surfaced during
 * checkout pre-validation (e.g. price drift beyond the allowed delta).
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutWarningDto {

    private UUID itemId;

    private UUID productId;

    private String warningType;

    private String message;
}
