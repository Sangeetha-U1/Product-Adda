package com.productadda.service.admin;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.product.AdminProductResubmissionResponseDto;
import com.productadda.entity.Product;
import com.productadda.entity.ProductStatus;
import com.productadda.exception.ApiException;
import com.productadda.repository.ProductRepository;
import com.productadda.repository.ProductStatusRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminProductResubmissionService {

    private final ProductRepository productRepository;
    private final ProductStatusRepository productStatusRepository;

    @Transactional
    public AdminProductResubmissionResponseDto resubmitProduct(UUID productId) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        if (productId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Product unique operational indicator must not be null");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED,
                    "Security context is missing raw authentication data");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ADMIN")
                        || a.getAuthority().equals("ROLE_SUPER_ADMIN")
                        || a.getAuthority().equals("SUPER_ADMIN"));

        if (!isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Access Denied: Only admins can access moderation endpoints");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Product not found with ID: " + productId));

        String currentStatus = product.getFkStatus() != null
                ? product.getFkStatus().getStatusCode()
                : "UNKNOWN";

        if (!"REJECTED".equalsIgnoreCase(currentStatus)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Only REJECTED products can be resubmitted");
        }

        ProductStatus pendingStatus = productStatusRepository.findByStatusCode("PENDING_APPROVAL")
                .orElseThrow(() -> new ApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Configured mapping target missing"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING
         * ================================================================
         */

        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

        product.setFkStatus(pendingStatus);
        product.setUpdatedAtUtc(nowUtc);

        Product savedProduct = productRepository.save(product);

        String adminEmail = authentication.getName();

        /*
         * ================================================================
         * 3. RESPONSE MAPPING
         * ================================================================
         */

        return AdminProductResubmissionResponseDto.builder()
                .productId(savedProduct.getPkProductId())
                .productName(savedProduct.getTitle())
                .status("PENDING_APPROVAL")
                .resubmittedAtUtc(nowUtc.toString())
                .resubmittedByAdmin(adminEmail)
                .build();
    }
}