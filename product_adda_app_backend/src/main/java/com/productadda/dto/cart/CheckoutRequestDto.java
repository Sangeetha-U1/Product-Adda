package com.productadda.dto.cart;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * ================================================================
 * REQUEST DTO
 * Description: Client-supplied checkout instructions. The shipping
 * method is optional; when omitted the cheapest active shipping
 * method is auto-selected server-side.
 * ================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDto {

    @NotNull(message = "IdempotencyKey uuid key is required")
    private UUID idempotencyKey;

    @NotNull(message = "Shipping address id is required")
    private UUID addressId;

    // Optional. When null, the cheapest active shipping method is
    // auto-selected by CheckoutPricingService.
    private UUID shippingMethodId;
}
