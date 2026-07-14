package com.productadda.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayBreakdownDto {

    private String paymentGateway;

    private Long count;

    private Long revenueInPaise;
}
