package com.productadda.service.cart;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.cart.CartTotalsDto;
import com.productadda.dto.cart.CheckoutValidateResponseDto;
import com.productadda.dto.cart.CheckoutWarningDto;
import com.productadda.dto.cart.CouponApplyRequestDto;
import com.productadda.dto.cart.CouponValidationResponseDto;

import com.productadda.entity.Address;
import com.productadda.entity.Cart;
import com.productadda.entity.CartItem;
import com.productadda.entity.CartStatus;
import com.productadda.entity.Inventory;
import com.productadda.entity.Product;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.AddressRepository;
import com.productadda.repository.CartItemRepository;
import com.productadda.repository.CartRepository;
import com.productadda.repository.CartStatusRepository;
import com.productadda.repository.InventoryRepository;
import com.productadda.repository.ProductRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckoutValidationService {

    private static final String ACTIVE_CART_STATUS = "ACTIVE";
    private static final BigDecimal PRICE_DELTA_WARNING_THRESHOLD = BigDecimal.valueOf(10);

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartStatusRepository cartStatusRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final AddressRepository addressRepository;
    private final CouponValidationService couponValidationService;
    private final CheckoutPricingService checkoutPricingService;

    @Transactional(readOnly = true)
    public CheckoutValidateResponseDto validateCart() {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: Pre-checkout validation. Throws on hard failures
         * (empty cart, insufficient stock, invalid coupon, no address).
         * Price drift beyond the threshold is captured as a warning only.
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        // Not applicable: this endpoint takes no request body.

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
        }

        String email;
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = authentication.getName();
        }

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        CartStatus activeStatus = cartStatusRepository.findByStatusCode(ACTIVE_CART_STATUS)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lookup cart status 'ACTIVE' not found"));

        Cart cart = cartRepository.findByFkUserAndFkCartStatusAndIsActiveTrue(user, activeStatus)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No active cart found for the authenticated user."));

        List<CartItem> cartItems = cartItemRepository.findByFkCartAndIsActiveTrue(cart);
        if (cartItems.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cart is empty.");
        }

        List<Address> userAddresses = addressRepository.findByFkUserAndIsActiveTrue(user);
        if (userAddresses.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "No shipping address provided. Add one before checkout.");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */

        // 2.1 Stock check per item (hard failure).
        List<CheckoutWarningDto> warnings = new ArrayList<>();

        for (CartItem item : cartItems) {

            Product product = productRepository.findById(item.getFkProduct().getPkProductId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));

            Inventory inventory = inventoryRepository.findByFkProduct(product)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Inventory track missing for target product"));

            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "Item " + product.getTitle() + " has insufficient stock. Only "
                                + inventory.getAvailableQuantity() + " available.");
            }

            // 2.2 Price drift check (soft failure -> warning, never blocks checkout).
            BigDecimal priceAtAdd = item.getPriceAtAdd();
            BigDecimal currentPrice = product.getPrice();

            BigDecimal deltaPercentage = BigDecimal.ZERO;
            if (priceAtAdd.compareTo(BigDecimal.ZERO) > 0) {
                deltaPercentage = currentPrice.subtract(priceAtAdd)
                        .divide(priceAtAdd, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .abs();
            }

            if (deltaPercentage.compareTo(PRICE_DELTA_WARNING_THRESHOLD) > 0) {
                warnings.add(CheckoutWarningDto.builder()
                        .itemId(item.getPkCartItemId())
                        .productId(product.getPkProductId())
                        .warningType("PRICE_CHANGED")
                        .message("Price changed by " + deltaPercentage.setScale(2, RoundingMode.HALF_UP)
                                + "% since this item was added to the cart.")
                        .build());
            }
        }

        // 2.3 Coupon re-validation (hard failure if applied coupon is no longer valid).
        if (cart.getFkCoupon() != null) {
            CouponValidationResponseDto couponValidation = couponValidationService.validateCoupon(
                    CouponApplyRequestDto.builder()
                            .couponCode(cart.getFkCoupon().getCouponCode())
                            .build());

            if (!Boolean.TRUE.equals(couponValidation.getValid())) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Applied coupon is no longer valid: " + couponValidation.getReason());
            }
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * Reason: Pre-validation never mutates state.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        // TODO: /checkout/validate has no addressId in its contract (per Day 5 spec),
        // so the tax region is estimated off the user's first active address.
        // The actual POST /api/checkout call always uses the address explicitly
        // supplied in CheckoutRequestDto, so this is an estimate only.
        CartTotalsDto estimatedTotals = checkoutPricingService.calculateCheckoutTotals(
                cartItems, cart.getFkCoupon(), userAddresses.get(0), null);

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return CheckoutValidateResponseDto.builder()
                .valid(true)
                .warnings(warnings)
                .estimatedTotals(estimatedTotals)
                .build();
    }
}
