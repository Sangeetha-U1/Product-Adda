package com.productadda.dto.customer;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * RESPONSE DTO
 * Description: Paginated wrapper around the authenticated
 * customer's DELIVERED-only order history.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedCustomerDeliveryHistoryResponseDto {

    private List<CustomerDeliveryHistoryListDto> content;
    private long totalElements;
    private int page;
    private int size;
}
