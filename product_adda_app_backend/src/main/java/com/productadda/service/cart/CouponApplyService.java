package com.productadda.service.cart;

import com.productadda.dto.cart.CartResponseDto;
import com.productadda.dto.cart.CartItemResponseDto;
import com.productadda.dto.cart.CartTotalsDto;
import com.productadda.dto.cart.CouponApplyRequestDto;

import com.productadda.entity.Cart;
import com.productadda.entity.CartItem;
import com.productadda.entity.CartStatus;
import com.productadda.entity.Coupon;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.CartItemRepository;
import com.productadda.repository.CartRepository;
import com.productadda.repository.CartStatusRepository;
import com.productadda.repository.CouponRepository;
import com.productadda.repository.CouponUsageHistoryRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponApplyService {

    private static final String ACTIVE_CART_STATUS = "ACTIVE";

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartStatusRepository cartStatusRepository;
    private final CouponRepository couponRepository;
    private final CouponUsageHistoryRepository couponUsageHistoryRepository;
    private final UserRepository userRepository;
    private final CartPricingService cartPricingService;

    @Transactional
    public CartResponseDto applyCoupon(CouponApplyRequestDto requestDto) {
        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: Performs manual checks on incoming DTO parameter payload,
         * validates context security authentication, and executes database lookups.
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (requestDto == null || requestDto.getCouponCode() == null || requestDto.getCouponCode().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Coupon code is mandatory.");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null || email.equals("anonymousUser")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User context is unauthenticated.");
        }

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user not found."));

        CartStatus activeStatus = cartStatusRepository.findByStatusCode(ACTIVE_CART_STATUS)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Lookup cart status 'ACTIVE' not found"));

        Cart cart = cartRepository.findByFkUserAndFkCartStatusAndIsActiveTrue(user, activeStatus)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No active cart found for this user."));

        if (cart.getFkCoupon() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "A coupon is already applied to this cart. Remove it first.");
        }

        String normalizedCode = requestDto.getCouponCode().trim().toUpperCase();

        Coupon coupon = couponRepository.findByCouponCodeAndIsActiveTrue(normalizedCode)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Coupon code not found or invalid."));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        LocalDateTime executionTime = LocalDateTime.now(ZoneOffset.UTC);

        if (!coupon.getIsActive() || !"ACTIVE".equals(coupon.getFkCouponStatus().getStatusCode())) {
            throw new ApiException(HttpStatus.CONFLICT, "Coupon is currently inactive.");
        }

        if (coupon.getExpiresAtUtc() != null && coupon.getExpiresAtUtc().isBefore(executionTime)) {
            throw new ApiException(HttpStatus.CONFLICT, "Coupon validity has expired.");
        }

        if (coupon.getIsOneTime() != null && coupon.getIsOneTime() && coupon.getUsageCount() >= 1) {
            throw new ApiException(HttpStatus.CONFLICT, "This single-use coupon has already been redeemed.");
        }

        if (coupon.getMaximumGlobalUsage() != null && coupon.getUsageCount() >= coupon.getMaximumGlobalUsage()) {
            throw new ApiException(HttpStatus.CONFLICT, "Coupon global usage limit has been reached.");
        }

        long userUsageCount = couponUsageHistoryRepository.countByFkCouponAndFkUser(coupon, user);
        
        if (coupon.getMaximumUserUsage() != null && userUsageCount >= coupon.getMaximumUserUsage()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "You have exceeded the maximum allowed usage limit for this coupon.");
        }

        List<CartItem> items = cartItemRepository.findByFkCartAndIsActiveTrue(cart);

        // Minimum purchase validation is handled by CartPricingService.

        cart.setFkCoupon(coupon);
        cart.setUpdatedAtUtc(executionTime);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        cartRepository.save(cart);

        /*
         * ================================================================
         * 4. PRICING ENGINE INTEGRATION (SINGLE SOURCE OF TRUTH)
         * ================================================================
         */
        CartTotalsDto summaryTotals = cartPricingService.calculateTotals(items, cart);

        List<CartItemResponseDto> mappedItems = items.stream().map(item -> CartItemResponseDto.builder()
                .itemId(item.getPkCartItemId())
                .productId(item.getFkProduct().getPkProductId())
                .quantity(item.getQuantity())
                .priceAtAdd(item.getPriceAtAdd())
                .currentPrice(item.getFkProduct().getPrice())
                .priceDeltaPercentage(java.math.BigDecimal.ZERO)
                .priceAlert(false)
                .build()).toList();

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return CartResponseDto.builder()
                .cartId(cart.getPkCartId())
                .status("ACTIVE")
                .createdAt(cart.getCreatedAtUtc())
                .expiresAt(null)
                .items(mappedItems)
                .totals(summaryTotals)
                .lastUpdated(cart.getUpdatedAtUtc())
                .build();
    }
}