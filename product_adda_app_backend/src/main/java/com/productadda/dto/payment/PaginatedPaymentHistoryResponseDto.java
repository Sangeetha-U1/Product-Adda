package com.productadda.dto.payment;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * RESPONSE DTO : PaginatedPaymentHistoryResponseDto
 * Sanitized, frontend-ready paginated wrapper around the
 * authenticated customer's payment history. Mirrors
 * PaginatedCustomerOrderResponseDto's shape exactly.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedPaymentHistoryResponseDto {

    private List<PaymentHistoryItemDto> content;

    private long totalElements;

    private int page;

    private int size;
}
