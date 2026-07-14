package com.productadda.controller.admin;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.admin.AdminOrderStatusUpdateRequestDto;
import com.productadda.dto.admin.AdminOrderStatusUpdateResponseDto;
import com.productadda.dto.admin.PaginatedAdminOrderResponseDto;
import com.productadda.dto.order.OrderCancellationRequestDto;
import com.productadda.dto.order.OrderCancellationResponseDto;
import com.productadda.service.admin.AdminOrderForceStatusTransitionService;
import com.productadda.service.admin.AdminOrderRetrievalService;
import com.productadda.service.order.OrderCancellationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminOrderController {

        private final AdminOrderRetrievalService adminOrderRetrievalService;
        private final OrderCancellationService orderCancellationService;
        private final AdminOrderForceStatusTransitionService adminOrderForceStatusTransitionService;

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

        @PutMapping("/api/admin/orders/{orderId}/force-cancel")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<OrderCancellationResponseDto>> forceCancelOrder(
                        @PathVariable UUID orderId,
                        @RequestBody OrderCancellationRequestDto request) {

                String reason = (request != null) ? request.getCancellationReason() : null;

                OrderCancellationResponseDto response = orderCancellationService.forceCancelOrder(orderId, reason);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<OrderCancellationResponseDto>builder()
                                                .success(true)
                                                .message("Order force-cancelled successfully")
                                                .data(response)
                                                .build());
        }

        @PutMapping("/api/admin/orders/{orderId}/status")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<AdminOrderStatusUpdateResponseDto>> updateOrderStatus(
                        @PathVariable UUID orderId,
                        @Valid @RequestBody AdminOrderStatusUpdateRequestDto request) {

                AdminOrderStatusUpdateResponseDto response = adminOrderForceStatusTransitionService
                                .forceStatusTransition(orderId, request.getRequestedStatus(), request.getReason());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<AdminOrderStatusUpdateResponseDto>builder()
                                                .success(true)
                                                .message("Order status updated successfully")
                                                .data(response)
                                                .build());
        }

}
