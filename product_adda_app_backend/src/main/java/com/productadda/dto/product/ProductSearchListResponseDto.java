package com.productadda.dto.product;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchListResponseDto {
    private List<ProductSearchResponseDto> products;
    private long totalProducts;
    private int currentPage;
    private int pageSize;
}