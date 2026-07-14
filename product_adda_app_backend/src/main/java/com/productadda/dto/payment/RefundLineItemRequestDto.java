package com.productadda.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundLineItemRequestDto {

    @NotBlank(message = "Order item id is required")
    private String orderItemId;

    @NotNull(message = "Refund amount is required")
    @Positive(message = "Refund amount must be greater than zero")
    private Long refundAmountInPaise;
}
