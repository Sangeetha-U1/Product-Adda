package com.productadda.dto.payment;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAnalyticsResponseDto {

    private Long totalTransactions;

    private Long totalRevenueInPaise;

    private Long totalRefundedInPaise;

    private Long netRevenueInPaise;

    private Double successRatePercent;

    private Double failedRatePercent;

    private List<PaymentMethodBreakdownDto> methodBreakdown;

    private List<PaymentGatewayBreakdownDto> gatewayBreakdown;

    private List<RevenueTrendPointDto> revenueTrend;
}
