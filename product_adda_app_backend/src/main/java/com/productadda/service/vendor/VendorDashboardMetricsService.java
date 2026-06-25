package com.productadda.service.vendor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.vendor.VendorDashboardMetricsResponseDto;
import com.productadda.entity.User;
import com.productadda.entity.Vendor;
import com.productadda.exception.ApiException;
import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.ProductRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorDashboardMetricsService {

        private final UserRepository userRepository;
        private final VendorRepository vendorRepository;
        private final ProductRepository productRepository;
        private final OrderItemRepository orderItemRepository;

        /*
         * ================================================================
         * GET DASHBOARD METRICS
         * Description: Compiles aggregated product and order metrics via
         * isolated merchant scope queries.
         * ================================================================
         */
        @Transactional(readOnly = true)
        public VendorDashboardMetricsResponseDto getMetrics() {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Validates security principal identities and verifies
                 * underlying vendor layer constraints from database records.
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                // Pure contextual analytical database stream. No raw payload parameters to
                // validate.

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                if (SecurityContextHolder.getContext().getAuthentication() == null ||
                                SecurityContextHolder.getContext().getAuthentication().getName() == null ||
                                SecurityContextHolder.getContext().getAuthentication().getName().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Principal system execution identity missing");
                }
                String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                User userInstance = userRepository.findByEmail(currentUserEmail)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user profile not found"));

                Vendor vendorInstance = vendorRepository.findByFkUser(userInstance)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "No associated vendor profile found for this user account"));

                /*
                 * ================================================================
                 * 2. BUSINESS SECTION
                 * Description: Compiles aggregated metric counts directly via database-level
                 * scanning functions for the isolated merchant scope.
                 * ================================================================
                 */
                long totalProducts = productRepository.countByFkVendor(vendorInstance);
                long totalOrderItems = orderItemRepository.countByFkProductFkVendor(vendorInstance);
                long totalDistinctOrders = orderItemRepository.countDistinctOrdersByVendor(vendorInstance);

                /*
                 * ================================================================
                 * 4. RESPONSE SECTION
                 * Description: Maps performance analytics metrics into the matching
                 * presentation response DTO payload directly inline.
                 * ================================================================
                 */
                return VendorDashboardMetricsResponseDto.builder()
                                .vendorId(vendorInstance.getPkVendorId())
                                .totalProducts(totalProducts)
                                .totalOrderItems(totalOrderItems)
                                .totalDistinctOrders(totalDistinctOrders)
                                .isActive(vendorInstance.getIsActive())
                                .build();
        }
}