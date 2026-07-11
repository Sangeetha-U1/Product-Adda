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
 * Description: Payload for a delivery partner rejecting a PENDING
 * assignment. rejectionReason is mandatory here (unlike the shared
 * cancellation DTO from Day 2, this is a single-purpose DTO used by
 * only one endpoint, so bean validation can be strict).
 * ================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAssignmentRejectRequestDto {

    @NotBlank(message = "rejectionReason cannot be blank")
    @Size(max = 500, message = "rejectionReason must not exceed 500 characters")
    private String rejectionReason;
}
