package com.productadda.controller.vendor;

import com.productadda.dto.product.ProductCreateRequestDto;
import com.productadda.dto.product.ProductImageDto;
import com.productadda.dto.product.ProductResponseDto;
import com.productadda.dto.product.ProductUpdateRequestDto;
import com.productadda.dto.product.ProductUpdateResponseDto;
import com.productadda.dto.vendor.VendorProductCatalogResponseDto;
import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.service.product.ProductCreateService;
import com.productadda.service.product.ProductRetrievalService;
import com.productadda.service.product.ProductUpdateService;
import com.productadda.service.product.ProductDeleteService;
import com.productadda.service.product.ProductImageService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VendorProductController {

        private final ProductCreateService productCreateService;
        private final ProductRetrievalService productRetrievalService;
        private final ProductUpdateService productUpdateService;
        private final ProductDeleteService productDeleteService;
        private final ProductImageService productImageService;

        @PostMapping("/products")
        @PreAuthorize("hasAnyAuthority('VENDOR')")
        public ResponseEntity<ApiSuccessResponseDto<ProductResponseDto>> createProduct(
                        @Valid @RequestBody ProductCreateRequestDto request) {

                ProductResponseDto responseData = productCreateService.createProduct(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiSuccessResponseDto.<ProductResponseDto>builder()
                                                .success(true)
                                                .message("Product created successfully and pending approval")
                                                .data(responseData)
                                                .build());
        }

        @PatchMapping("/products/{productId}")
        @PreAuthorize("hasAnyAuthority('VENDOR')")
        public ResponseEntity<ApiSuccessResponseDto<ProductUpdateResponseDto>> updateProduct(
                        @PathVariable UUID productId,
                        @Valid @RequestBody ProductUpdateRequestDto request) {

                ProductUpdateResponseDto responseData = productUpdateService.updateProduct(productId, request);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<ProductUpdateResponseDto>builder()
                                                .success(true)
                                                .message("Product updated successfully")
                                                .data(responseData)
                                                .build());
        }

        @DeleteMapping("/products/{productId}")
        @PreAuthorize("hasAnyAuthority('VENDOR')")
        public ResponseEntity<ApiSuccessResponseDto<ProductResponseDto>> deleteProduct(
                        @PathVariable UUID productId) {

                ProductResponseDto responseData = productDeleteService.deleteProduct(productId);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<ProductResponseDto>builder()
                                                .success(true)
                                                .message("Product deleted successfully and marked as DELETED")
                                                .data(responseData)
                                                .build());
        }

        @GetMapping("/vendors/products")
        @PreAuthorize("hasAnyAuthority('VENDOR')")
        public ResponseEntity<ApiSuccessResponseDto<VendorProductCatalogResponseDto>> getVendorProducts(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                VendorProductCatalogResponseDto responseData = productRetrievalService.getVendorProducts(page, size);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto
                                                .<VendorProductCatalogResponseDto>builder()
                                                .success(true)
                                                .message("Vendor products retrieved successfully")
                                                .data(responseData)
                                                .build());
        }

        @PostMapping("/products/{productId}/images")
        @PreAuthorize("hasAnyAuthority('VENDOR')")
        public ResponseEntity<ApiSuccessResponseDto<ProductImageDto>> uploadProductImage(
                        @PathVariable UUID productId,
                        @RequestParam("image") MultipartFile file) {

                ProductImageDto responseData = productImageService.uploadProductImage(productId, file);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiSuccessResponseDto.<ProductImageDto>builder()
                                                .success(true)
                                                .message("Product image uploaded successfully")
                                                .data(responseData)
                                                .build());
        }
}