package com.productadda.service.product;

import com.productadda.dto.product.ProductResponseDto;

import com.productadda.entity.Product;
import com.productadda.entity.ProductStatus;
import com.productadda.entity.User;
import com.productadda.entity.Vendor;

import com.productadda.exception.ApiException;

import com.productadda.repository.ProductRepository;
import com.productadda.repository.ProductStatusRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductDeleteService {

    private final ProductRepository productRepository;
    private final ProductStatusRepository productStatusLookupRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;

    @Transactional
    public ProductResponseDto deleteProduct(UUID productId) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: Zero-Trust validation for product soft delete
         * ================================================================
         */

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User authentication context missing");
        }

        boolean isVendor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR") || a.getAuthority().equals("VENDOR"));

        if (!isVendor) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied: Only vendors can delete products");
        }

        String currentUsername = authentication.getName();

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        User user = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        Vendor vendor = vendorRepository.findByFkUser(user)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Vendor profile not found"));

        if (vendor.getIsActive() == null || !vendor.getIsActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied: Vendor account is inactive");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Product not found with ID: " + productId));

        // Ownership check
        if (!product.getFkVendor().getPkVendorId().equals(vendor.getPkVendorId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied: You can only delete your own products");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING
         * ================================================================
         */
        ProductStatus deletedStatus = productStatusLookupRepository.findByStatusCode("DELETED")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "DELETED status configuration missing"));

        product.setFkStatus(deletedStatus);

        // Set isActive = false for soft delete consistency across the system
        product.setIsActive(false);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        Product savedProduct = productRepository.save(product);

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return ProductResponseDto.builder()
                .productId(savedProduct.getPkProductId())
                .vendorId(vendor.getPkVendorId())
                .productName(savedProduct.getTitle())
                .status(savedProduct.getFkStatus().getStatusCode())
                .createdAtUtc(java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT))
                .build();
    }
}