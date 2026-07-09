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
 * the full-visibility admin order list.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedAdminOrderResponseDto {

    private List<AdminOrderDetailDto> content;

    private long totalElements;

    private int page;

    private int size;
}
