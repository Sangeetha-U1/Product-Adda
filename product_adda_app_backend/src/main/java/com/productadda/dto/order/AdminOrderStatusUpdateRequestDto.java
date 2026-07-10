package com.productadda.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * ================================================================
 * REQUEST DTO
 * Description: Payload for an admin-initiated force status
 * transition. requestedStatus must be non-blank; the exact set of
 * legal values (and transition rules based on current state) is
 * re-validated manually in the service layer.
 * ================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderStatusUpdateRequestDto {

    @NotBlank(message = "requestedStatus is required")
    private String requestedStatus;

    @Size(max = 500, message = "reason must not exceed 500 characters")
    private String reason;
}
