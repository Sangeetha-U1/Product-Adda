package com.productadda.service.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.product.AdminProductDataWrapper;
import com.productadda.dto.product.AdminProductListResponseDto;
import com.productadda.entity.Product;
import com.productadda.exception.ApiException;
import com.productadda.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminProductRetrievalService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public AdminProductDataWrapper getAllProducts(int page, int size) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        if (page < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Page index parameters must not be negative indices");
        }

        if (size <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Page batch size parameter must be greater than zero");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED,
                    "Security context is missing raw authentication data");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SUPER_ADMIN")
                        || a.getAuthority().equals("ADMIN"));

        if (!isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Access Denied: Only admins can access product administration endpoints");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING
         * ================================================================
         */

        Pageable pageable = PageRequest.of(page, size);

        Page<Product> productPage = productRepository.findAllNonDeletedProducts(pageable);

        List<AdminProductListResponseDto> products = productPage
                .map(product -> AdminProductListResponseDto.builder()
                        .productId(product.getPkProductId())
                        .productName(product.getTitle())
                        .description(product.getDescription())
                        .vendorId(product.getFkVendor() != null
                                ? product.getFkVendor().getPkVendorId()
                                : null)
                        .vendorName(product.getFkVendor() != null
                                ? product.getFkVendor().getBusinessName()
                                : null)
                        .categoryName(product.getFkCategory() != null
                                ? product.getFkCategory().getCategoryName()
                                : null)
                        .brandName(product.getFkBrand() != null
                                ? product.getFkBrand().getBrandName()
                                : null)
                        .price(product.getPrice())
                        .status(product.getFkStatus() != null
                                ? product.getFkStatus().getStatusCode()
                                : "UNKNOWN")
                        .createdAtUtc(product.getCreatedAtUtc() != null
                                ? product.getCreatedAtUtc().toString()
                                : null)
                        .build())
                .getContent();

        /*
         * ================================================================
         * 3. RESPONSE MAPPING
         * ================================================================
         */

        return AdminProductDataWrapper.builder()
                .products(products)
                .totalProducts(productPage.getTotalElements())
                .currentPage(page)
                .pageSize(size)
                .build();
    }
}