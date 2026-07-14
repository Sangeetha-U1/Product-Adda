package com.productadda.controller.deliverypartner;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.deliverypartner.DeliveryPartnerClaimResponseDto;
import com.productadda.dto.deliverypartner.DeliveryPartnerCompleteResponseDto;
import com.productadda.dto.deliverypartner.DeliveryPartnerPickupResponseDto;
import com.productadda.dto.deliverypartner.PaginatedDeliveryPartnerAvailableOrderResponseDto;
import com.productadda.dto.deliverypartner.PaginatedDeliveryPartnerMyOrderResponseDto;
import com.productadda.dto.order.DeliveryOtpSendResponseDto;
import com.productadda.dto.order.DeliveryOtpVerifyRequestDto;

import com.productadda.service.deliverypartners.DeliveryPartnerSendDeliveryOtpService;
import com.productadda.service.deliverypartners.DeliveryPartnerPickupOrderService;
import com.productadda.service.deliverypartners.DeliveryPartnerDeliveredOrderService;
import com.productadda.service.deliverypartners.DeliveryPartnerGetAvailableOrdersService;
import com.productadda.service.deliverypartners.DeliveryPartnerGetMyOrdersService;
import com.productadda.service.deliverypartners.DeliveryPartnerGetOrdersDeliveryHistoryService;
import com.productadda.service.deliverypartners.DeliveryPartnerOutfordeliveryOrderService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DeliveryPartnerController {

        private final DeliveryPartnerPickupOrderService deliveryPartnerPickupOrderService;
        private final DeliveryPartnerGetAvailableOrdersService deliveryPartnerOrdersRetrievalService;
        private final DeliveryPartnerGetMyOrdersService deliveryPartnerGetMyOrdersService;
        private final DeliveryPartnerOutfordeliveryOrderService deliveryPartnerOutfordeliveryOrderService;
        private final DeliveryPartnerDeliveredOrderService deliveryPartnerDeliveredOrderService;
        private final DeliveryPartnerGetOrdersDeliveryHistoryService deliveryPartnerGetOrdersDeliveryHistoryService;
        private final DeliveryPartnerSendDeliveryOtpService deliveryPartnerSendDeliveryOtpService;

        // ==========================================
        // GET AVAILABLE BROWSE ORDERS FOR PICKUP
        // Description: Exposes unassigned SHIPPED orders to validated delivery
        // partners.
        // ==========================================
        @GetMapping("/api/delivery-partners/available-orders")
        @PreAuthorize("hasAuthority('DELIVERY_PARTNER')")
        public ResponseEntity<ApiSuccessResponseDto<PaginatedDeliveryPartnerAvailableOrderResponseDto>> getAvailableOrders(
                        @Valid @org.springframework.data.web.PageableDefault(size = 10, page = 0) Pageable pageable) {

                PaginatedDeliveryPartnerAvailableOrderResponseDto responseData = deliveryPartnerOrdersRetrievalService
                                .getAvailableOrders(pageable);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<PaginatedDeliveryPartnerAvailableOrderResponseDto>builder()
                                                .success(true)
                                                .message("Available pickup orders retrieved successfully")
                                                .data(responseData)
                                                .build());
        }

        // ==========================================
        // CLAIM ENDPOINT
        // Description: PROCESSING -> PICKED_UP
        // ==========================================
        @PutMapping("/api/delivery-partners/orders/{orderId}/pickup")
        @PreAuthorize("hasAuthority('DELIVERY_PARTNER')")
        public ResponseEntity<ApiSuccessResponseDto<DeliveryPartnerClaimResponseDto>> claimOrder(
                        @Valid @PathVariable UUID orderId) {

                DeliveryPartnerClaimResponseDto response = deliveryPartnerPickupOrderService.pickupOrder(orderId);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<DeliveryPartnerClaimResponseDto>builder()
                                                .success(true)
                                                .message("Assignment accepted. Delivery is on the way!")
                                                .data(response)
                                                .build());
        }

        // ==========================================
        // GET ACTIVE ("MY") ORDERS FOR AUTHENTICATED DELIVERY PARTNER
        // Description: Orders already claimed by this partner across
        // PICKED_UP, OUT_FOR_DELIVERY, DELIVERED statuses.
        // ==========================================
        @GetMapping("/api/delivery-partners/my-orders")
        @PreAuthorize("hasAuthority('DELIVERY_PARTNER')")
        public ResponseEntity<ApiSuccessResponseDto<PaginatedDeliveryPartnerMyOrderResponseDto>> getMyOrders(
                        @Valid @org.springframework.data.web.PageableDefault(size = 10, page = 0) Pageable pageable) {

                PaginatedDeliveryPartnerMyOrderResponseDto responseData = deliveryPartnerGetMyOrdersService
                                .getMyOrders(pageable);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<PaginatedDeliveryPartnerMyOrderResponseDto>builder()
                                                .success(true)
                                                .message("Active delivery orders retrieved successfully")
                                                .data(responseData)
                                                .build());
        }

        // ==========================================
        // PICKUP CONFIRMATION
        // Description: PICKED_UP -> OUT_FOR_DELIVERY
        // ==========================================
        @PutMapping("/api/delivery-partners/orders/{orderId}/outfordelivery")
        @PreAuthorize("hasAuthority('DELIVERY_PARTNER')")
        public ResponseEntity<ApiSuccessResponseDto<DeliveryPartnerPickupResponseDto>> confirmPickup(
                        @Valid @PathVariable UUID orderId) {

                DeliveryPartnerPickupResponseDto response = deliveryPartnerOutfordeliveryOrderService
                                .outforDeliveryOrder(orderId);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<DeliveryPartnerPickupResponseDto>builder()
                                                .success(true)
                                                .message("Order marked as out for delivery")
                                                .data(response)
                                                .build());
        }

        // ==========================================
        // SEND / RESEND DELIVERY OTP
        // Description: Partner hits this at the customer's door. Calling
        // it again re-sends a fresh OTP (acts as resend).
        // ==========================================
        @PutMapping("/api/delivery-partners/orders/{orderId}/send-delivery-otp")
        @PreAuthorize("hasAuthority('DELIVERY_PARTNER')")
        public ResponseEntity<ApiSuccessResponseDto<DeliveryOtpSendResponseDto>> sendDeliveryOtp(
                        @Valid @PathVariable UUID orderId) {

                DeliveryOtpSendResponseDto response = deliveryPartnerSendDeliveryOtpService.sendDeliveryOtp(orderId);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<DeliveryOtpSendResponseDto>builder()
                                                .success(true)
                                                .message("Delivery OTP sent to customer")
                                                .data(response)
                                                .build());
        }

        // ==========================================
        // DELIVERY COMPLETION
        // Description: OUT_FOR_DELIVERY -> DELIVERED
        // ==========================================
        @PutMapping("/api/delivery-partners/orders/{orderId}/delivered")
        @PreAuthorize("hasAuthority('DELIVERY_PARTNER')")
        public ResponseEntity<ApiSuccessResponseDto<DeliveryPartnerCompleteResponseDto>> confirmDelivery(
                        @Valid @PathVariable UUID orderId,
                        @RequestBody DeliveryOtpVerifyRequestDto request) {

                DeliveryPartnerCompleteResponseDto response = deliveryPartnerDeliveredOrderService
                                .deliveredOrder(orderId, request.getOtp());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<DeliveryPartnerCompleteResponseDto>builder()
                                                .success(true)
                                                .message("Delivery completed successfully")
                                                .data(response)
                                                .build());
        }

        // ==========================================
        // DELIVERY HISTORY
        // Description: DELIVERED-only orders for this partner.
        // ==========================================
        @GetMapping("/api/delivery-partners/delivery-history")
        @PreAuthorize("hasAuthority('DELIVERY_PARTNER')")
        public ResponseEntity<ApiSuccessResponseDto<PaginatedDeliveryPartnerMyOrderResponseDto>> getDeliveryHistory(
                        @Valid @org.springframework.data.web.PageableDefault(size = 10, page = 0) Pageable pageable) {

                PaginatedDeliveryPartnerMyOrderResponseDto responseData = deliveryPartnerGetOrdersDeliveryHistoryService
                                .getDeliveryHistory(pageable);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<PaginatedDeliveryPartnerMyOrderResponseDto>builder()
                                                .success(true)
                                                .message("Delivery history retrieved successfully")
                                                .data(responseData)
                                                .build());
        }
}
