package com.productadda.dto.product;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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
public class ProductCreateRequestDto {

    @NotBlank(message = "Product name is required and cannot be blank")
    private String productName;

    private String description;

    @NotBlank(message = "Product SKU code is required and cannot be blank")
    private String skuCode;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotNull(message = "Brand ID is required")
    private UUID brandId;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be a positive value greater than zero")
    private BigDecimal price;

    private BigDecimal discountPrice;
    private Integer stockQuantity;
}