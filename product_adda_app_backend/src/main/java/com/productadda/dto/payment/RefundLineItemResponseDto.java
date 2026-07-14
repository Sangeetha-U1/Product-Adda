package com.productadda.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundLineItemResponseDto {

    private String orderItemId;

    private String productNameSnapshot;

    private Long refundAmountInPaise;
}
