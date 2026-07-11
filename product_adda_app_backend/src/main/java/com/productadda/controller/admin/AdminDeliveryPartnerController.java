package com.productadda.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.order.DeliveryPartnerCreateRequestDto;
import com.productadda.dto.order.DeliveryPartnerResponseDto;

import com.productadda.service.order.DeliveryPartnerManagementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDeliveryPartnerController {

    private final DeliveryPartnerManagementService deliveryPartnerManagementService;

    // ==========================================
    // DELIVERY PARTNER ONBOARDING ENDPOINT
    // ==========================================
    @PostMapping("/delivery-partners")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiSuccessResponseDto<DeliveryPartnerResponseDto>> createDeliveryPartner(
            @Valid @RequestBody DeliveryPartnerCreateRequestDto request) {

        DeliveryPartnerResponseDto response = deliveryPartnerManagementService.createDeliveryPartner(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiSuccessResponseDto.<DeliveryPartnerResponseDto>builder()
                        .success(true)
                        .message("Delivery partner created successfully")
                        .data(response)
                        .build());
    }
}
