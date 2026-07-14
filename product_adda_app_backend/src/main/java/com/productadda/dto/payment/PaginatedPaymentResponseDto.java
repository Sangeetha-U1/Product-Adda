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
public class PaginatedPaymentResponseDto {

    private List<PaymentListItemDto> payments;
    private long totalCount;
    private int page;
    private int pageSize;
}