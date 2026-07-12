package com.productadda.controller.order;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.order.DeliveryAssignmentRejectRequestDto;
import com.productadda.dto.order.DeliveryAssignmentRejectResponseDto;
import com.productadda.dto.order.DeliveryAssignmentResponseDto;
import com.productadda.dto.order.PaginatedDeliveryPartnerAvailableOrderResponseDto;
import com.productadda.service.order.DeliveryPartnerAcceptanceService;
import com.productadda.service.order.DeliveryPartnerOrdersRetrievalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DeliveryPartnerController {

        private final DeliveryPartnerAcceptanceService deliveryPartnerAcceptanceService;
        private final DeliveryPartnerOrdersRetrievalService deliveryPartnerOrdersRetrievalService;

        // TODO: // When the partner picks up the package from the vendor/hub
        // PUT /api/delivery-partners/assignments/{assignmentId}/pickup -> Sets status
        // to OUT_FOR_DELIVERY

        // When the partner hands the package to the customer
        // PUT /api/delivery-partners/assignments/{assignmentId}/complete -> Sets status
        // to DELIVERED

        // ==========================================
        // GET AVAILABLE BROWSE ORDERS FOR PICKUP
        // Description: Exposes unassigned SHIPPED orders to validated delivery
        // partners.
        // ==========================================
        @GetMapping("/api/delivery-partners/available-orders")
        @PreAuthorize("hasAuthority('DELIVERY_PARTNER')")
        public ResponseEntity<ApiSuccessResponseDto<PaginatedDeliveryPartnerAvailableOrderResponseDto>> getAvailableOrders(
                        @org.springframework.data.web.PageableDefault(size = 10, page = 0) Pageable pageable) {

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
        // ACCEPT ASSIGNMENT ENDPOINT
        // ==========================================
        @PutMapping("/api/delivery-partners/assignments/{assignmentId}/accept")
        @PreAuthorize("hasAuthority('DELIVERY_PARTNER')")
        public ResponseEntity<ApiSuccessResponseDto<DeliveryAssignmentResponseDto>> acceptAssignment(
                        @PathVariable UUID assignmentId) {

                DeliveryAssignmentResponseDto response = deliveryPartnerAcceptanceService
                                .acceptAssignment(assignmentId);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<DeliveryAssignmentResponseDto>builder()
                                                .success(true)
                                                .message("Assignment accepted. Delivery is on the way!")
                                                .data(response)
                                                .build());
        }

        // ==========================================
        // REJECT ASSIGNMENT ENDPOINT
        // ==========================================
        @PutMapping("/api/delivery-partners/assignments/{assignmentId}/reject")
        @PreAuthorize("hasAuthority('DELIVERY_PARTNER')")
        public ResponseEntity<ApiSuccessResponseDto<DeliveryAssignmentRejectResponseDto>> rejectAssignment(
                        @PathVariable UUID assignmentId,
                        @Valid @RequestBody DeliveryAssignmentRejectRequestDto request) {

                DeliveryAssignmentRejectResponseDto response = deliveryPartnerAcceptanceService
                                .rejectAssignment(assignmentId, request.getRejectionReason());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<DeliveryAssignmentRejectResponseDto>builder()
                                                .success(true)
                                                .message("Assignment rejected successfully. Order flagged for re-assignment.")
                                                .data(response)
                                                .build());
        }
}
