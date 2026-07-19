package com.productadda.dto.product;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponseDto {
    private UUID productId;
    private String productName;
    private String description;
    private String categoryName;
    private String brandName;
    private Integer price;
    private String status;
    private List<String> imageUrls;
    private Integer stock;
}