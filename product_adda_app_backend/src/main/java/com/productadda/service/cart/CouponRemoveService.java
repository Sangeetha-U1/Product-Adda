package com.productadda.service.cart;

import com.productadda.dto.cart.CartResponseDto;
import com.productadda.dto.cart.CartItemResponseDto;
import com.productadda.dto.cart.CartTotalsDto;

import com.productadda.entity.Cart;
import com.productadda.entity.CartItem;
import com.productadda.entity.CartStatus;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.CartItemRepository;
import com.productadda.repository.CartRepository;
import com.productadda.repository.CartStatusRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponRemoveService {
        private static final String ACTIVE_CART_STATUS = "ACTIVE";

        private final CartRepository cartRepository;
        private final CartItemRepository cartItemRepository;
        private final CartStatusRepository cartStatusRepository;
        private final UserRepository userRepository;

        @Transactional
        public CartResponseDto removeCoupon() {
                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Validates user principal context, database presence of the
                 * active shopper profile, and checks if a coupon assignment exists.
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                // No inline modification payload parameters required for coupon removal method
                // signature.

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
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user not found."));

                CartStatus activeStatus = cartStatusRepository.findByStatusCode(ACTIVE_CART_STATUS)
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.NOT_FOUND,
                                                "Lookup cart status 'ACTIVE' not found"));
                // Match exact CartRepository signature method name matching active code
                // tracking attributes
                Cart cart = cartRepository.findByFkUserAndFkCartStatusAndIsActiveTrue(user, activeStatus)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "No active cart found for this user."));

                if (cart.getFkCoupon() == null) {
                        throw new ApiException(HttpStatus.CONFLICT, "No applied coupon found on this cart.");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                LocalDateTime executionTime = LocalDateTime.now(ZoneOffset.UTC);
                cart.setFkCoupon(null);
                cart.setUpdatedAtUtc(executionTime);

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION (Skip if Read-Only GET)
                 * ================================================================
                 */
                cartRepository.save(cart);

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * ================================================================
                 */
                List<CartItem> items = cartItemRepository.findByFkCartAndIsActiveTrue(cart);
                BigDecimal subtotal = BigDecimal.ZERO;
                for (CartItem item : items) {
                        subtotal = subtotal.add(item.getPriceAtAdd().multiply(BigDecimal.valueOf(item.getQuantity())));
                }

                BigDecimal baseShipping = BigDecimal.valueOf(15.00);
                BigDecimal baseTaxRate = BigDecimal.valueOf(0.08);
                BigDecimal taxAmount = subtotal.multiply(baseTaxRate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal totalAmount = subtotal.add(baseShipping).add(taxAmount).setScale(2, RoundingMode.HALF_UP);

                List<CartItemResponseDto> mappedItems = items.stream().map(item -> CartItemResponseDto.builder()
                                // Match exact CartItemResponseDto item index identifier field property mapping
                                .itemId(item.getPkCartItemId())
                                .productId(item.getFkProduct().getPkProductId())
                                .quantity(item.getQuantity())
                                .priceAtAdd(item.getPriceAtAdd())
                                .currentPrice(item.getFkProduct().getPrice())
                                .priceDeltaPercentage(BigDecimal.ZERO)
                                .priceAlert(false)
                                .build()).toList();

                CartTotalsDto summaryTotals = CartTotalsDto.builder()
                                .subtotal(subtotal)
                                .couponDiscount(BigDecimal.ZERO)
                                .shippingCost(baseShipping)
                                .taxAmount(taxAmount)
                                .total(totalAmount)
                                .build();

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * ================================================================
                 */
                // TODO: Refactor timestamp serialization to enforce strict UTC ISO-8601 format
                // with 'Z' suffix (e.g., 2026-07-04T14:43:17Z) to match API specifications.
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