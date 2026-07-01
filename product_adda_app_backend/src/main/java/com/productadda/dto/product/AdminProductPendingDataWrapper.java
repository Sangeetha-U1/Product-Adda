package com.productadda.dto.product;

import java.util.List;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductPendingDataWrapper {
    private List<AdminProductPendingListResponseDto> products;
    private long totalPendingProducts;
    private int currentPage;
    private int pageSize;
}