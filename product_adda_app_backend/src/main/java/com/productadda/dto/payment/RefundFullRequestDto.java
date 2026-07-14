package com.productadda.dto.payment;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

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
public class RefundFullRequestDto {

    @NotBlank(message = "Payment id is required")
    private UUID paymentId;

    @NotBlank(message = "Order id is required")
    private UUID orderId;

    // One of: cancellation, return, manual_admin
    @NotBlank(message = "Reason is required")
    private String reason;

    // Optional: when the 30-day refund window has already closed, an admin
    // must explicitly set this to true to proceed
    private Boolean adminApprovalOverride;
}
