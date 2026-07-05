package com.productadda.dto.cart;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponApplyRequestDto {

    /*
     * ================================================================
     * PROMOTIONAL CODE IDENTIFIER
     * ================================================================
     */
    @NotBlank(message = "Coupon code cannot be blank")
    private String couponCode;
}