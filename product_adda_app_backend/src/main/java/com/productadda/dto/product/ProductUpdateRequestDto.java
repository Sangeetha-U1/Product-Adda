package com.productadda.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.openapitools.jackson.nullable.JsonNullable;

import java.math.BigDecimal;
import java.util.UUID;

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
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class ProductUpdateRequestDto {

    @Builder.Default
    private JsonNullable<String> productName = JsonNullable.undefined();

    @Builder.Default
    private JsonNullable<String> description = JsonNullable.undefined();

    @Builder.Default
    private JsonNullable<String> skuCode = JsonNullable.undefined();

    @Builder.Default
    private JsonNullable<UUID> categoryId = JsonNullable.undefined();

    @Builder.Default
    private JsonNullable<UUID> brandId = JsonNullable.undefined();

    @Builder.Default
    private JsonNullable<BigDecimal> price = JsonNullable.undefined();

    @Builder.Default
    private JsonNullable<BigDecimal> discountPrice = JsonNullable.undefined();
}