package com.productadda.dto.order;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * ================================================================
 * NESTED ITEM DTO (Request-side)
 * Description: A single entry inside an ItemStatusBatchRequestDto's
 * items list, pairing one order item with its requested new status.
 * ================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemStatusBatchEntryDto {

    @NotNull(message = "itemId is required")
    private UUID itemId;

    @NotBlank(message = "requestedStatus is required")
    private String requestedStatus;
}
