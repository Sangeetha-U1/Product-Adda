package com.productadda.controller.customer;

import java.util.UUID;

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
import com.productadda.dto.customer.PaginatedCustomerDeliveryHistoryResponseDto;
import com.productadda.dto.customer.PaginatedCustomerOrderResponseDto;
import com.productadda.dto.order.OrderCancellationRequestDto;
import com.productadda.dto.order.OrderCancellationResponseDto;
import com.productadda.service.order.OrderCancellationService;
import com.productadda.service.customer.CustomerGetCustomerOrdersService;
import com.productadda.service.customer.CustomerGetOrdersDeliveryHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CustomerOrderController {

        private final CustomerGetCustomerOrdersService customerGetCustomerOrdersService;
        private final CustomerGetOrdersDeliveryHistoryService customerGetOrdersDeliveryHistoryService;
        private final OrderCancellationService orderCancellationService;

        @GetMapping("/api/customers/orders")
        @PreAuthorize("hasAuthority('CUSTOMER')")
        public ResponseEntity<ApiSuccessResponseDto<PaginatedCustomerOrderResponseDto>> getCustomerOrders(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate) {

                PaginatedCustomerOrderResponseDto response = customerGetCustomerOrdersService
                                .getCustomerOrders(page, size, status, startDate, endDate);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<PaginatedCustomerOrderResponseDto>builder()
                                                .success(true)
                                                .message("Orders retrieved successfully")
                                                .data(response)
                                                .build());
        }

        // ==========================================
        // DELIVERY HISTORY
        // Description: DELIVERED-only orders for the authenticated customer.
        // ==========================================
        @GetMapping("/api/customers/orders/delivery-history")
        @PreAuthorize("hasAuthority('CUSTOMER')")
        public ResponseEntity<ApiSuccessResponseDto<PaginatedCustomerDeliveryHistoryResponseDto>> getDeliveryHistory(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {

                PaginatedCustomerDeliveryHistoryResponseDto response = customerGetOrdersDeliveryHistoryService
                                .getDeliveryHistory(page, size);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<PaginatedCustomerDeliveryHistoryResponseDto>builder()
                                                .success(true)
                                                .message("Delivery history retrieved successfully")
                                                .data(response)
                                                .build());
        }

        @PutMapping("/api/customers/orders/{orderId}/cancel")
        @PreAuthorize("hasAuthority('CUSTOMER')")
        public ResponseEntity<ApiSuccessResponseDto<OrderCancellationResponseDto>> cancelOrder(
                        @PathVariable UUID orderId,
                        @RequestBody(required = false) OrderCancellationRequestDto request) {

                String reason = (request != null) ? request.getCancellationReason() : null;

                OrderCancellationResponseDto response = orderCancellationService.cancelCustomerOrder(orderId, reason);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<OrderCancellationResponseDto>builder()
                                                .success(true)
                                                .message("Order cancelled successfully")
                                                .data(response)
                                                .build());
        }
}
