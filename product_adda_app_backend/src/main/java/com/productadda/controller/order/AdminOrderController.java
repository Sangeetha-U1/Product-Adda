package com.productadda.controller.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.order.PaginatedAdminOrderResponseDto;

import com.productadda.service.order.AdminOrderRetrievalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderRetrievalService adminOrderRetrievalService;

    // ==========================================
    // FULL-VISIBILITY ADMIN ORDER RETRIEVAL
    // ==========================================
    @GetMapping("/api/admin/orders")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiSuccessResponseDto<PaginatedAdminOrderResponseDto>> getAdminOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String vendorId,
            @RequestParam(required = false) String deliveryPartnerId,
            @RequestParam(required = false) String createdAfter,
            @RequestParam(required = false) String createdBefore) {

        PaginatedAdminOrderResponseDto response = adminOrderRetrievalService.getAdminOrders(
                page, size, status, userId, vendorId, deliveryPartnerId, createdAfter, createdBefore);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponseDto.<PaginatedAdminOrderResponseDto>builder()
                        .success(true)
                        .message("Admin order records fetched successfully")
                        .data(response)
                        .build());
    }
}
