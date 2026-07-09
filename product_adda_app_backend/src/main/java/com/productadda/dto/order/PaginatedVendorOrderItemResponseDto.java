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
 * the authenticated vendor's order-item view.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedVendorOrderItemResponseDto {

    private List<VendorOrderItemListDto> content;

    private long totalElements;

    private int page;

    private int size;
}
