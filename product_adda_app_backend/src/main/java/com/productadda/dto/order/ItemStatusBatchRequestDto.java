package com.productadda.dto.order;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * ================================================================
 * REQUEST DTO
 * Description: Batch payload for updating multiple order items in a
 * single vendor request. All entries are validated up-front before
 * any mutation is applied (all-or-nothing), per the Flattened Linear
 * Workflow Pattern and transactional atomicity standards.
 * ================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemStatusBatchRequestDto {

    @NotEmpty(message = "items list must not be empty")
    @Valid
    private List<ItemStatusBatchEntryDto> items;
}
