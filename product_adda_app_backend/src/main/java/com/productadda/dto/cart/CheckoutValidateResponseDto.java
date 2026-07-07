package com.productadda.dto.cart;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * RESPONSE DTO
 * Description: Sanitized, frontend-ready payload returned by the
 * checkout pre-validation endpoint. Never blocks on warnings,
 * only on hard failures raised as ApiException upstream.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutValidateResponseDto {

    private Boolean valid;

    private List<CheckoutWarningDto> warnings;

    private CartTotalsDto estimatedTotals;
}
