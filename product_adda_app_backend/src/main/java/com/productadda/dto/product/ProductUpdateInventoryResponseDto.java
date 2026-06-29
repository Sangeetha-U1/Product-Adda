package com.productadda.dto.product;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateInventoryResponseDto {
    private ProductInventoryResponseDto inventory;

    private Map<String, Object> updatedFields;
}
