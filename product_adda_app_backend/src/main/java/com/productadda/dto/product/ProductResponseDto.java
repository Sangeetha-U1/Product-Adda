package com.productadda.dto.product;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {

    private UUID productId;
    private UUID vendorId;
    private String productName;
    private String description;
    private String skuCode;
    private UUID categoryId;
    private UUID brandId;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stockQuantity;
    private String status;
    private List<String> imageUrls;
    private String createdAtUtc;
}