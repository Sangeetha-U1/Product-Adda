package com.productadda.dto.order;

import java.lang.Long;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * NESTED ITEM DTO
 * Description: Read-only payment snapshot embedded inside order
 * detail responses. Null when no payment has been initiated yet
 * (checkout intentionally does not create a payment record).
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentSummaryDto {

    private UUID paymentId;

    private String statusName;

    private String paymentMethod;

    private Long amountPaidInPaise;

    private LocalDateTime paidAtUtc;
}
