package com.productadda.controller.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.order.PaginatedVendorOrderItemResponseDto;

import com.productadda.service.order.VendorOrderRetrievalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class VendorOrderController {

    private final VendorOrderRetrievalService vendorOrderRetrievalService;

    // ==========================================
    // VENDOR-SCOPED ORDER ITEM RETRIEVAL
    // ==========================================
    @GetMapping("/api/vendors/orders")
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ApiSuccessResponseDto<PaginatedVendorOrderItemResponseDto>> getVendorOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PaginatedVendorOrderItemResponseDto response = vendorOrderRetrievalService.getVendorOrders(page, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponseDto.<PaginatedVendorOrderItemResponseDto>builder()
                        .success(true)
                        .message("Vendor orders retrieved successfully")
                        .data(response)
                        .build());
    }
}
