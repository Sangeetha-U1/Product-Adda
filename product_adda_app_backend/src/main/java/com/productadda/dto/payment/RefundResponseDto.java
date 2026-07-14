package com.productadda.dto.payment;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponseDto {

    private String refundId;

    private String paymentId;

    private String orderId;

    // "full" or "partial"
    private String refundType;

    private Long refundAmountInPaise;

    private String status;

    private String gatewayRefundId;

    private String initiatedBy;

    private String reason;

    private LocalDateTime createdAtUtc;

    private LocalDateTime processedAtUtc;

    // Only populated when status = FAILED
    private String failureReason;

    // Only populated when refundType = "partial"
    private List<RefundLineItemResponseDto> lineItems;
}
