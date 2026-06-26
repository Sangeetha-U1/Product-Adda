package com.productadda.service.product;

import com.productadda.dto.vendor.VendorProductCatalogResponseDto;
import com.productadda.dto.vendor.VendorProductItemDto;
import com.productadda.entity.*;
import com.productadda.repository.*;
import com.productadda.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductRetrievalService {

        private final ProductRepository productRepository;
        private final UserRepository userRepository;
        private final VendorRepository vendorRepository;
        private final InventoryRepository inventoryRepository;

        @Transactional(readOnly = true)
        public VendorProductCatalogResponseDto getVendorProducts(int page, int size) {
                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Context identity resolution and bounds validation checks
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (page < 0) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Page index parameter bounds cannot sit below 0");
                }
                if (size <= 0) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Page size allocations must measure strictly greater than 0 entries");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "User authentication context missing");
                }

                // Hardened programmatic security authority containment check
                boolean isVendor = authentication.getAuthorities().stream()
                                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_VENDOR")
                                                || grantedAuthority.getAuthority().equals("VENDOR"));

                if (!isVendor) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Access Denied: Security Principal context is missing required VENDOR permissions");
                }

                String currentUsername = authentication.getName();

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                User user = userRepository.findByEmail(currentUsername)
                                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                                                "Authenticated user identity record matching session metadata not found"));

                Vendor vendor = vendorRepository.findByFkUser(user)
                                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN,
                                                "Access Denied: Only active vendors can pull context details"));

                if (vendor.getIsActive() == null || !vendor.getIsActive()) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Access Denied: Associated vendor profile status is currently inactive");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * Description: Pulls structural content nodes across lazy segments utilizing
                 * custom joins
                 * ================================================================
                 */
                Page<Product> productPage = productRepository.findByFkVendor(vendor, PageRequest.of(page, size));

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * Description: Iterates targets and embeds explicit metadata loops sequentially
                 * without nested models
                 * ================================================================
                 */
                List<VendorProductItemDto> productItems = productPage.getContent().stream().map(product -> {
                        int stockLevel = inventoryRepository.findByFkProduct(product)
                                        .map(Inventory::getAvailableQuantity)
                                        .orElse(0);

                        String createdAtString = product.getCreatedAtUtc() != null
                                        ? product.getCreatedAtUtc().atOffset(java.time.ZoneOffset.UTC)
                                                        .format(DateTimeFormatter.ISO_INSTANT)
                                        : java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
                                                        .format(DateTimeFormatter.ISO_INSTANT);

                        return VendorProductItemDto.builder()
                                        .productId(product.getPkProductId())
                                        .productName(product.getTitle())
                                        .description(product.getDescription())
                                        .skuCode(product.getSku())
                                        .categoryName(product.getFkCategory().getCategoryName())
                                        .brandName(product.getFkBrand().getBrandName())
                                        .price(product.getPrice())
                                        .status(product.getFkStatus().getStatusCode())
                                        .stock(stockLevel)
                                        .createdAtUtc(createdAtString)
                                        .build();
                }).collect(Collectors.toList());

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * Description: Wraps collection metadata elements tightly inside clean response
                 * frames
                 * ================================================================
                 */
                return VendorProductCatalogResponseDto.builder()
                                .products(productItems)
                                .totalProducts(productPage.getTotalElements())
                                .currentPage(page)
                                .pageSize(size)
                                .build();
        }
}