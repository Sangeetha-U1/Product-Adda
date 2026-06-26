package com.productadda.dto.vendor;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorProductCatalogResponseDto {
    private List<VendorProductItemDto> products;
    private long totalProducts;
    private int currentPage;
    private int pageSize;
}