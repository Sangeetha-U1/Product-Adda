package com.productadda.controller.vendor;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.productadda.dto.order.ItemStatusBatchRequestDto;
import com.productadda.dto.order.ItemStatusBatchResponseDto;
import com.productadda.dto.order.ItemStatusUpdateRequestDto;
import com.productadda.dto.order.OrderItemResponseDto;
import com.productadda.dto.vendor.PaginatedVendorOrderItemResponseDto;
import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.service.vendor.VendorOrderRetrievalService;
import com.productadda.service.vendor.VendorOrderStatusBatchTransitionService;
import com.productadda.service.vendor.VendorOrderStatusTransitionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class VendorOrderController {

        private final VendorOrderRetrievalService vendorOrderRetrievalService;
        private final VendorOrderStatusTransitionService vendorOrderStatusTransitionService;
        private final VendorOrderStatusBatchTransitionService vendorOrderStatusBatchTransitionService;

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

        @PutMapping("/api/vendors/orders/{orderId}/items/{itemId}/status")
        @PreAuthorize("hasAuthority('VENDOR')")
        public ResponseEntity<ApiSuccessResponseDto<OrderItemResponseDto>> updateItemStatus(
                        @PathVariable UUID orderId,
                        @PathVariable UUID itemId,
                        @Valid @RequestBody ItemStatusUpdateRequestDto request) {

                OrderItemResponseDto response = vendorOrderStatusTransitionService
                                .updateVendorItemStatus(orderId, itemId, request.getRequestedStatus());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<OrderItemResponseDto>builder()
                                                .success(true)
                                                .message("Item status updated successfully")
                                                .data(response)
                                                .build());
        }

        @PutMapping("/api/vendors/orders/{orderId}/items/status/batch")
        @PreAuthorize("hasAuthority('VENDOR')")
        public ResponseEntity<ApiSuccessResponseDto<ItemStatusBatchResponseDto>> batchUpdateItemStatus(
                        @PathVariable UUID orderId,
                        @Valid @RequestBody ItemStatusBatchRequestDto request) {

                ItemStatusBatchResponseDto response = vendorOrderStatusBatchTransitionService
                                .batchUpdateVendorItemStatus(orderId, request.getItems());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<ItemStatusBatchResponseDto>builder()
                                                .success(true)
                                                .message("Batch item status update completed successfully")
                                                .data(response)
                                                .build());
        }
}
