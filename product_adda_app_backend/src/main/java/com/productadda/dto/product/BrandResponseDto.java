package com.productadda.dto.product;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponseDto {
    private UUID brandId;
    private String brandName;
    private String logoUrl;
}