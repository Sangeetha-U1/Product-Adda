package com.productadda.dto.product;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import org.openapitools.jackson.nullable.JsonNullable;

import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class ProductUpdateInventoryRequestDto {

    @Builder.Default
    private JsonNullable<Integer> currentStock = JsonNullable.undefined();

    @Builder.Default
    private JsonNullable<Integer> reorderLevel = JsonNullable.undefined();

    @Builder.Default
    private JsonNullable<Integer> maxStock = JsonNullable.undefined();
}