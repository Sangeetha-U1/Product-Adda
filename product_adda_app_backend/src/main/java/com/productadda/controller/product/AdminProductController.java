package com.productadda.controller.product;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.product.AdminModerationStatsResponseDto;
import com.productadda.dto.product.AdminProductApprovalResponseDto;
import com.productadda.dto.product.AdminProductPendingDataWrapper;
import com.productadda.dto.product.AdminProductRejectionRequestDto;
import com.productadda.service.product.AdminProductApprovalService;
import com.productadda.service.product.AdminProductModerationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

        private final AdminProductModerationService adminProductModerationService;
        private final AdminProductApprovalService adminProductApprovalService;

        @GetMapping("/pending")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<AdminProductPendingDataWrapper>> getPendingProducts(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                AdminProductPendingDataWrapper responseData = adminProductModerationService.getPendingProducts(page,
                                size);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<AdminProductPendingDataWrapper>builder()
                                                .success(true)
                                                .message("Pending products retrieved successfully")
                                                .data(responseData)
                                                .build());
        }

        @PutMapping("/{productId}/approve")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<AdminProductApprovalResponseDto>> approveProduct(
                        @PathVariable UUID productId) {

                AdminProductApprovalResponseDto responseData = adminProductApprovalService.approveProduct(productId);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<AdminProductApprovalResponseDto>builder()
                                                .success(true)
                                                .message("Product approved successfully")
                                                .data(responseData)
                                                .build());
        }

        @PutMapping("/{productId}/reject")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<AdminProductApprovalResponseDto>> rejectProduct(
                        @PathVariable UUID productId,
                        @Valid @RequestBody AdminProductRejectionRequestDto request) {

                AdminProductApprovalResponseDto responseData = adminProductApprovalService.rejectProduct(productId,
                                request);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<AdminProductApprovalResponseDto>builder()
                                                .success(true)
                                                .message("Product rejected successfully")
                                                .data(responseData)
                                                .build());
        }

        @GetMapping("/moderation/stats")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<AdminModerationStatsResponseDto>> getModerationStats() {

                AdminModerationStatsResponseDto responseData = adminProductModerationService.getModerationStatistics();

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<AdminModerationStatsResponseDto>builder()
                                                .success(true)
                                                .message("Moderation statistics retrieved successfully")
                                                .data(responseData)
                                                .build());
        }
}