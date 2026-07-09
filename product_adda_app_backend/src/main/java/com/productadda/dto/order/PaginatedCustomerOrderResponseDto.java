package com.productadda.dto.order;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * RESPONSE DTO
 * Description: Sanitized, frontend-ready paginated wrapper around
 * the authenticated customer's order list.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedCustomerOrderResponseDto {

    private List<CustomerOrderListDto> content;

    private long totalElements;

    private int page;

    private int size;
}
