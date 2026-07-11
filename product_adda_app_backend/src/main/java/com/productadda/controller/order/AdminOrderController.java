package com.productadda.controller.order;

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
import org.springframework.web.bind.annotation.PostMapping;

import com.productadda.dto.order.DeliveryAssignmentRequestDto;
import com.productadda.dto.order.DeliveryAssignmentResponseDto;
import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.order.PaginatedAdminOrderResponseDto;
import com.productadda.dto.order.OrderCancellationRequestDto;
import com.productadda.dto.order.OrderCancellationResponseDto;
import com.productadda.dto.order.AdminOrderStatusUpdateRequestDto;
import com.productadda.dto.order.AdminOrderStatusUpdateResponseDto;

import com.productadda.service.order.OrderCancellationService;
import com.productadda.service.order.AdminOrderRetrievalService;
import com.productadda.service.order.OrderStatusTransitionService;
import com.productadda.service.order.DeliveryAssignmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminOrderController {

        private final AdminOrderRetrievalService adminOrderRetrievalService;
        private final OrderCancellationService orderCancellationService;
        private final OrderStatusTransitionService orderStatusTransitionService;
        private final DeliveryAssignmentService deliveryAssignmentService;

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

                AdminOrderStatusUpdateResponseDto response = orderStatusTransitionService
                                .forceStatusTransition(orderId, request.getRequestedStatus(), request.getReason());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<AdminOrderStatusUpdateResponseDto>builder()
                                                .success(true)
                                                .message("Order status updated successfully")
                                                .data(response)
                                                .build());
        }

        @PostMapping("/api/admin/orders/{orderId}/assign-delivery-partner")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<DeliveryAssignmentResponseDto>> assignDeliveryPartner(
                        @PathVariable UUID orderId,
                        @Valid @RequestBody DeliveryAssignmentRequestDto request) {

                DeliveryAssignmentResponseDto response = deliveryAssignmentService
                                .assignDeliveryPartnerToOrder(orderId, request.getDeliveryPartnerId());

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiSuccessResponseDto.<DeliveryAssignmentResponseDto>builder()
                                                .success(true)
                                                .message("Delivery partner assigned successfully. Awaiting partner acceptance.")
                                                .data(response)
                                                .build());
        }
}
