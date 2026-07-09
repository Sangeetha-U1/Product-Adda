package com.productadda.service.cart;

import com.productadda.dto.cart.CouponApplyRequestDto;
import com.productadda.dto.cart.CouponValidationResponseDto;
import com.productadda.entity.Coupon;
import com.productadda.entity.User;

import com.productadda.repository.CouponRepository;
import com.productadda.repository.CouponUsageHistoryRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponValidationService {

    private final CouponRepository couponRepository;
    private final CouponUsageHistoryRepository couponUsageHistoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CouponValidationResponseDto validateCoupon(CouponApplyRequestDto requestDto) {
        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: Evaluates the raw incoming coupon validation payload structure
         * and attempts database matching.
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (requestDto == null || requestDto.getCouponCode() == null || requestDto.getCouponCode().trim().isEmpty()) {
            return CouponValidationResponseDto.builder()
                    .valid(false)
                    .reason("Coupon code parameter must be provided.")
                    .build();
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Optional<User> optionalUser = (email != null && !email.equals("anonymousUser"))
                ? userRepository.findByEmail(email)
                : Optional.empty();

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        String normalizedCode = requestDto.getCouponCode().trim().toUpperCase();
        // Match exact CouponRepository signature method name
        Optional<Coupon> optionalCoupon = couponRepository.findByCouponCodeAndIsActiveTrue(normalizedCode);

        if (optionalCoupon.isEmpty()) {
            return CouponValidationResponseDto.builder()
                    .valid(false)
                    .reason("Coupon code does not exist.")
                    .build();
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        Coupon coupon = optionalCoupon.get();
        LocalDateTime executionTime = LocalDateTime.now(ZoneOffset.UTC);

        // Match exact Coupon entity expiration timestamp field mapping attribute name
        if (coupon.getExpiresAtUtc() != null && coupon.getExpiresAtUtc().isBefore(executionTime)) {
            return CouponValidationResponseDto.builder()
                    .valid(false)
                    .reason("Coupon has expired.")
                    .expiresAt(coupon.getExpiresAtUtc())
                    .build();
        }

        // Match exact CouponStatus entity attribute field mapping
        if (!coupon.getIsActive() || !"ACTIVE".equals(coupon.getFkCouponStatus().getStatusCode())) {
            return CouponValidationResponseDto.builder()
                    .valid(false)
                    .reason("Coupon is currently marked inactive.")
                    .build();
        }

        // Handle one-time usage constraints explicitly during validation
        if (coupon.getIsOneTime() != null && coupon.getIsOneTime() && coupon.getUsageCount() >= 1) {
            return CouponValidationResponseDto.builder()
                    .valid(false)
                    .reason("This single-use coupon has already been redeemed.")
                    .build();
        }

        if (coupon.getMaximumGlobalUsage() != null && coupon.getUsageCount() >= coupon.getMaximumGlobalUsage()) {
            return CouponValidationResponseDto.builder()
                    .valid(false)
                    .reason("Global registration limits for coupon exhausted.")
                    .build();
        }

        if (optionalUser.isPresent()) {
            long userUsageCount = couponUsageHistoryRepository.countByFkCouponAndFkUser(coupon, optionalUser.get());
            if (coupon.getMaximumUserUsage() != null && userUsageCount >= coupon.getMaximumUserUsage()) {
                return CouponValidationResponseDto.builder()
                        .valid(false)
                        .reason("Maximum usage frequency limit reached for this specific user.")
                        .build();
            }
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * ================================================================
         */
        // Omitted: Database persistence routines skipped within a read-only
        // transactional execution context.

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        // Omitted: Sanitization operations are bypassed because lookups extract
        // immutable declarative schemas.

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return CouponValidationResponseDto.builder()
                .valid(true)
                .message("Coupon satisfies all active promotional system state queries.")
                // Match exact CouponDiscountType entity key name matching structural
                // definitions
                .discountType(coupon.getFkDiscountType().getDiscountTypeCode())
                .discountValue(coupon.getDiscountValue())
                // Match exact property fields on Coupon entity
                .maxDiscountAmount(coupon.getMaximumDiscountAmount())
                .minPurchaseAmount(coupon.getMinimumPurchaseAmount())
                .expiresAt(coupon.getExpiresAtUtc())
                .build();
    }
}