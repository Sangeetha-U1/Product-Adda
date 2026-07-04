package com.productadda.dto.wishlist;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemRequestDto {

    /*
     * =========================================================
     * REQUEST PAYLOAD ATTRIBUTES
     * =========================================================
     */
    @NotNull(message = "Product identifier is required and cannot be null")
    private UUID productId;
}