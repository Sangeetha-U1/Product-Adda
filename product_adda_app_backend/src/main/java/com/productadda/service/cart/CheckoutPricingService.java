package com.productadda.service.cart;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.productadda.dto.cart.CartTotalsDto;

import com.productadda.entity.Address;
import com.productadda.entity.CartItem;
import com.productadda.entity.Coupon;
import com.productadda.entity.ShippingMethod;
import com.productadda.entity.TaxConfiguration;

import com.productadda.exception.ApiException;

import com.productadda.repository.ShippingMethodRepository;
import com.productadda.repository.TaxConfigurationRepository;

import lombok.RequiredArgsConstructor;

// TODO: ShippingMethod.costPerKg is intentionally not applied because Product
// has no weight column yet. Only baseCost is used until weight is added.

@Service
@RequiredArgsConstructor
public class CheckoutPricingService {

    private static final String DEFAULT_TAX_REGION = "DEFAULT";

    private final ShippingMethodRepository shippingMethodRepository;
    private final TaxConfigurationRepository taxConfigurationRepository;

    public CartTotalsDto calculateCheckoutTotals(
            List<CartItem> items,
            Coupon coupon,
            Address address,
            UUID shippingMethodIdOverride) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (items == null || items.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cart items cannot be null or empty");
        }

        if (address == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Shipping address is required for total calculation");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        // Not applicable (pure pricing service, called by an authenticated caller).

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        ShippingMethod shippingMethod;
        if (shippingMethodIdOverride != null) {
            shippingMethod = shippingMethodRepository.findById(shippingMethodIdOverride)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Selected shipping method not found"));

            if (!Boolean.TRUE.equals(shippingMethod.getIsActive())) {
                throw new ApiException(HttpStatus.CONFLICT, "Selected shipping method is no longer active");
            }
        } else {
            shippingMethod = shippingMethodRepository.findByIsActiveTrue()
                    .stream()
                    .min(Comparator.comparing(ShippingMethod::getBaseCost))
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "No active shipping methods configured"));
        }

        TaxConfiguration taxConfiguration = taxConfigurationRepository
                .findByRegionNameAndIsActiveTrue(address.getState())
                .orElseGet(() -> taxConfigurationRepository
                        .findByRegionNameAndIsActiveTrue(DEFAULT_TAX_REGION)
                        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "No tax configuration found for region or DEFAULT fallback")));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        BigDecimal subtotal = BigDecimal.ZERO;
        int itemCount = 0;

        for (CartItem item : items) {
            subtotal = subtotal.add(item.getPriceAtAdd().multiply(BigDecimal.valueOf(item.getQuantity())));
            itemCount += item.getQuantity();
        }

        BigDecimal discount = BigDecimal.ZERO;

        if (coupon != null) {

            if (coupon.getMinimumPurchaseAmount() != null
                    && subtotal.compareTo(coupon.getMinimumPurchaseAmount()) < 0) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Minimum purchase amount of " + coupon.getMinimumPurchaseAmount().stripTrailingZeros().toPlainString()
                                + " is required to apply coupon '" + coupon.getCouponCode() + "'.");
            }

            String discountTypeCode = coupon.getFkDiscountType().getDiscountTypeCode();

            if ("FIXED".equalsIgnoreCase(discountTypeCode)) {
                discount = coupon.getDiscountValue();
            } else if ("PERCENTAGE".equalsIgnoreCase(discountTypeCode)) {
                discount = subtotal.multiply(
                        coupon.getDiscountValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            } else {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unsupported or malformed discount type code: " + discountTypeCode);
            }

            if (coupon.getMaximumDiscountAmount() != null
                    && discount.compareTo(coupon.getMaximumDiscountAmount()) > 0) {
                discount = coupon.getMaximumDiscountAmount();
            }

            if (discount.compareTo(subtotal) > 0) {
                discount = subtotal;
            }
        }

        // Shipping cost: base cost only until product weight is supported.
        BigDecimal shippingCost = shippingMethod.getBaseCost();

        BigDecimal taxableAmount = subtotal.subtract(discount).add(shippingCost);
        if (taxableAmount.compareTo(BigDecimal.ZERO) < 0) {
            taxableAmount = BigDecimal.ZERO;
        }

        BigDecimal taxAmount = taxableAmount
                .multiply(taxConfiguration.getTaxPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal
                .subtract(discount)
                .add(shippingCost)
                .add(taxAmount)
                .setScale(2, RoundingMode.HALF_UP);

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * Reason: Pure calculation service, no persistence.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        // Values already rounded above during calculation.

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return CartTotalsDto.builder()
                .itemCount(itemCount)
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .couponDiscount(discount.setScale(2, RoundingMode.HALF_UP))
                .shippingCost(shippingCost.setScale(2, RoundingMode.HALF_UP))
                .taxAmount(taxAmount)
                .total(total)
                .build();
    }
}
