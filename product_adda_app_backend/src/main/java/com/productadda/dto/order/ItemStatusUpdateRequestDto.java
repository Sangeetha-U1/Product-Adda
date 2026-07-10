package com.productadda.dto.order;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * ================================================================
 * REQUEST DTO
 * Description: Payload for a single vendor-initiated item status
 * transition. Only PROCESSING or SHIPPED are legal values here;
 * the exact legality of the transition (based on the item's CURRENT
 * status) is re-validated manually in the service layer, not via
 * bean validation, since it depends on existing DB state.
 * ================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemStatusUpdateRequestDto {

    @NotBlank(message = "requestedStatus is required")
    private String requestedStatus;
}
