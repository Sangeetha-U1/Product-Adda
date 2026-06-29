package com.productadda.dto.product;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventoryResponseDto {

    private UUID inventoryId;
    private UUID productId;
    private Integer currentStock;
    private Integer reorderLevel;
    private Integer maxStock;
    private Boolean isLowStock;
    private Boolean availableForSale;
    private ZonedDateTime createdAtUtc;
    private ZonedDateTime updatedAtUtc;
}