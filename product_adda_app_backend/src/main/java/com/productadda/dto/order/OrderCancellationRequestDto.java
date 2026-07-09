package com.productadda.dto.order;

import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * ================================================================
 * REQUEST DTO
 * Description: Shared cancellation request payload used by BOTH the
 * customer self-cancel endpoint and the admin force-cancel endpoint.
 * cancellationReason is intentionally NOT annotated with @NotBlank
 * here, since the two call sites have conflicting requirements:
 *   - Customer cancellation: blank/omitted reason is allowed and
 *     defaults to "Customer requested cancellation" (see Day 2 test
 *     scenario 2, which sends an empty string and expects 200 OK).
 *   - Admin force-cancellation: a non-blank reason is REQUIRED.
 * Because a single shared DTO cannot carry two contradictory bean
 * validation rules for the same field, the blank/required check is
 * performed manually in each service's own Section 1.1, consistent
 * with this project's Zero-Trust manual-validation style.
 * ================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancellationRequestDto {

    @Size(max = 500, message = "cancellationReason must not exceed 500 characters")
    private String cancellationReason;
}
