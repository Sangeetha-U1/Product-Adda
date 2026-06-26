package com.productadda.dto.vendor;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorProductItemDto {
    private UUID productId;
    private String productName;
    private String description;
    private String skuCode;
    private String categoryName;
    private String brandName;
    private BigDecimal price;
    private String status;
    private Integer stock;
    private String createdAtUtc;
}