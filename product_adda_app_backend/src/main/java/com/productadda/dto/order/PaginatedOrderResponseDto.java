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
 * the authenticated user's order list.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedOrderResponseDto {

    private List<OrderListItemDto> orders;

    private long totalCount;

    private int page;

    private int pageSize;
}
