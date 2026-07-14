package com.productadda.dto.payment;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

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
public class RefundPartialRequestDto {

    @NotBlank(message = "Payment id is required")
    private String paymentId;

    @NotBlank(message = "Order id is required")
    private String orderId;

    // One of: cancellation, return, manual_admin
    @NotBlank(message = "Reason is required")
    private String reason;

    @NotEmpty(message = "At least one refund line item is required")
    @Valid
    private List<RefundLineItemRequestDto> refundLineItems;
}
