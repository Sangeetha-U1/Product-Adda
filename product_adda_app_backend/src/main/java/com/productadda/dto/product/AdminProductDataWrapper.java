package com.productadda.dto.product;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductDataWrapper {

    private List<AdminProductListResponseDto> products;
    private long totalProducts;
    private int currentPage;
    private int pageSize;
}