package com.productadda.controller.cart;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.cart.CheckoutRequestDto;
import com.productadda.dto.cart.CheckoutResponseDto;
import com.productadda.dto.cart.CheckoutValidateResponseDto;
import com.productadda.dto.cart.CheckoutShippingMethodResponseDto;

import com.productadda.service.cart.CheckoutShippingMethodService;
import com.productadda.service.cart.CheckoutService;
import com.productadda.service.cart.CheckoutValidationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// TODO: Handle CartReservationCleanupEvent-style cleanup for stale checkouts
// that fail mid-transaction, once inventory reserved/sold states are finalized.

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Cart checkout validation and order finalization")
public class CheckoutController {

        private final CheckoutValidationService checkoutValidationService;
        private final CheckoutService checkoutService;
        private final CheckoutShippingMethodService checkoutShippingMethodService;

        @Operation(summary = "Pre-validate the active cart before checkout", description = "Runs stock, coupon, and address checks without committing any data. "
                        + "Price drift beyond 10% is returned as a non-blocking warning.")
        @ApiResponse(responseCode = "200", description = "Cart passes pre-checkout validation")
        @PostMapping("/validate")
        public ResponseEntity<ApiSuccessResponseDto<CheckoutValidateResponseDto>> validateCheckout() {

                CheckoutValidateResponseDto response = checkoutValidationService.validateCart();

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<CheckoutValidateResponseDto>builder()
                                                .success(true)
                                                .message("Cart passed checkout validation")
                                                .data(response)
                                                .build());
        }

        @Operation(summary = "Complete checkout and create an order", description = "Atomically validates the cart, calculates final totals, creates the order, "
                        + "converts reserved inventory, and marks the cart completed. "
                        + "Supports idempotent retry via the Idempotency-Key in body.")
        @ApiResponse(responseCode = "201", description = "Order created successfully")
        @ApiResponse(responseCode = "200", description = "Idempotent replay: existing order returned unchanged")
        @PostMapping
        public ResponseEntity<ApiSuccessResponseDto<CheckoutResponseDto>> checkout(
                        @Parameter(description = "Client-generated UUID to guard against duplicate checkout submissions", required = true)
                        @Valid @RequestBody CheckoutRequestDto requestDto) {

                CheckoutResponseDto response = checkoutService.checkout(requestDto);

                // TODO: Differentiate 201 (newly created) vs 200 (idempotent replay) once
                // CheckoutService surfaces that distinction back to the controller.
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiSuccessResponseDto.<CheckoutResponseDto>builder()
                                                .success(true)
                                                .message("Checkout completed successfully")
                                                .data(response)
                                                .build());
        }

        @GetMapping("/shipping-methods")
        public ResponseEntity<ApiSuccessResponseDto<List<CheckoutShippingMethodResponseDto>>> getShippingMethods() {

                List<CheckoutShippingMethodResponseDto> response = checkoutShippingMethodService
                                .getActiveShippingMethods();

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<List<CheckoutShippingMethodResponseDto>>builder()
                                                .success(true)
                                                .message("Active shipping methods retrieved successfully")
                                                .data(response)
                                                .build());
        }
}
