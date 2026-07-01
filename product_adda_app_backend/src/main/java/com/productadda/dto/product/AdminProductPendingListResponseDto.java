package com.productadda.dto.product;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductPendingListResponseDto {
    private UUID productId;
    private String productName;
    private String description;
    private UUID vendorId;
    private String vendorName;
    private String categoryName;
    private String brandName;
    private BigDecimal price;
    private String status;
    private String createdAtUtc;
}