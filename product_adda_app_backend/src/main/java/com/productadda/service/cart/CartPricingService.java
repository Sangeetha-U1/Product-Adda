package com.productadda.service.cart;

import com.productadda.dto.cart.CartTotalsDto;
import com.productadda.entity.Cart;
import com.productadda.entity.CartItem;
import com.productadda.entity.Coupon;
import com.productadda.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

// TODO: Add caching (Redis/Caffeine) for calculateTotals(cartId, couponId)
// Invalidate cache on: add/update/remove/coupon apply/clear cart

@Service
public class CartPricingService {

    private static final BigDecimal SHIPPING_COST = BigDecimal.valueOf(15.00);
    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.08);

    public CartTotalsDto calculateTotals(List<CartItem> items, Cart cart) {

        BigDecimal subtotal = BigDecimal.ZERO;
        int itemCount = 0;

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (items == null || items.isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Cart items cannot be null or empty");
        }

        if (cart == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Cart cannot be null");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        // Not applicable (pure pricing service)

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        Coupon coupon = cart.getFkCoupon();

        if (coupon != null && coupon.getFkDiscountType() == null) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Invalid coupon configuration: missing discount type");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */

        // Subtotal calculation
        for (CartItem item : items) {
            subtotal = subtotal.add(
                    item.getPriceAtAdd()
                            .multiply(BigDecimal.valueOf(item.getQuantity())));
            itemCount += item.getQuantity();
        }

        BigDecimal discount = BigDecimal.ZERO;

        if (coupon != null) {

            if (coupon.getMinimumPurchaseAmount() != null
                    && subtotal.compareTo(coupon.getMinimumPurchaseAmount()) < 0) {

                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "Minimum purchase amount of ₹"
                                + coupon.getMinimumPurchaseAmount().stripTrailingZeros().toPlainString()
                                + " is required to apply coupon '"
                                + coupon.getCouponCode()
                                + "'.");
            }

            String type = coupon.getFkDiscountType().getDiscountTypeCode();

            // FIXED: Added .equalsIgnoreCase to catch lowercase types and added an else
            // block to prevent silent $0 discount bugs
            if ("FIXED".equalsIgnoreCase(type)) {
                discount = coupon.getDiscountValue();
            } else if ("PERCENTAGE".equalsIgnoreCase(type)) {
                discount = subtotal.multiply(
                        coupon.getDiscountValue()
                                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            } else {
                throw new ApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unsupported or malformed discount type code: " + type);
            }

            if (coupon.getMaximumDiscountAmount() != null &&
                    discount.compareTo(coupon.getMaximumDiscountAmount()) > 0) {
                discount = coupon.getMaximumDiscountAmount();
            }

            if (discount.compareTo(subtotal) > 0) {
                discount = subtotal;
            }
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * ================================================================
         */
        // No DB mutations in pricing service

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */

        BigDecimal taxable = subtotal.subtract(discount);
        if (taxable.compareTo(BigDecimal.ZERO) < 0) {
            taxable = BigDecimal.ZERO;
        }

        BigDecimal tax = taxable.multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = taxable
                .add(SHIPPING_COST)
                .add(tax)
                .setScale(2, RoundingMode.HALF_UP);

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */

        CartTotalsDto response = CartTotalsDto.builder()
                .itemCount(itemCount)
                .subtotal(subtotal)
                .couponDiscount(discount)
                .shippingCost(SHIPPING_COST)
                .taxAmount(tax)
                .total(total)
                .build();

        return response;
    }
}