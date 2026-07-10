package com.productadda.dto.order;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * RESPONSE DTO
 * Description: Sanitized, frontend-ready wrapper around the results
 * of a batch item status update, including a total updated count.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemStatusBatchResponseDto {

    private List<OrderItemResponseDto> items;

    private int updatedCount;
}
