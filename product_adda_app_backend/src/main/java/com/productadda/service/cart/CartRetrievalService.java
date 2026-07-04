package com.productadda.service.cart;

import com.productadda.dto.cart.CartItemResponseDto;
import com.productadda.dto.cart.CartResponseDto;
import com.productadda.dto.cart.CartTotalsDto;
import com.productadda.entity.Cart;
import com.productadda.entity.CartItem;
import com.productadda.entity.Product;
import com.productadda.exception.ApiException;
import com.productadda.repository.CartRepository;
import com.productadda.repository.CartItemRepository;
import com.productadda.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartRetrievalService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public CartResponseDto getCart(UUID cartId) {
        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: Verify structural identifier constraints, security scope, and
         * data tier records.
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (cartId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cart ID must be provided");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "JWT token is missing or invalid");
        }
        String currentUsername = authentication.getName();

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cart not found"));

        if (!cart.getFkUser().getEmail().equals(currentUsername)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to access this cart");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        List<CartItem> cartItems = cartItemRepository.findByFkCartAndIsActiveTrue(cart);
        List<CartItemResponseDto> itemDtos = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItemCount = 0;

        for (CartItem item : cartItems) {
            Product product = productRepository.findById(item.getFkProduct().getPkProductId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));

            BigDecimal priceAtAdd = item.getPriceAtAdd();
            BigDecimal currentPrice = product.getPrice();

            BigDecimal deltaPercentage = BigDecimal.ZERO;
            if (priceAtAdd.compareTo(BigDecimal.ZERO) > 0) {
                deltaPercentage = currentPrice.subtract(priceAtAdd)
                        .divide(priceAtAdd, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .abs();
            }

            boolean priceAlert = deltaPercentage.compareTo(BigDecimal.valueOf(10)) > 0;
            BigDecimal quantityVal = BigDecimal.valueOf(item.getQuantity());
            subtotal = subtotal.add(priceAtAdd.multiply(quantityVal));
            totalItemCount += item.getQuantity();

            // Zero-Helper Inline Mapping Rule strictly observed here via explicit builder
            // chaining
            itemDtos.add(CartItemResponseDto.builder()
                    .itemId(item.getPkCartItemId())
                    .productId(product.getPkProductId())
                    .quantity(item.getQuantity())
                    .priceAtAdd(priceAtAdd)
                    .currentPrice(currentPrice)
                    .priceDeltaPercentage(deltaPercentage.setScale(2, RoundingMode.HALF_UP))
                    .priceAlert(priceAlert)
                    .build());
        }

        BigDecimal couponDiscount = BigDecimal.ZERO;
        BigDecimal shippingCost = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.subtract(couponDiscount).add(shippingCost).add(taxAmount);

        CartTotalsDto totalsDto = CartTotalsDto.builder()
                .itemCount(totalItemCount)
                .subtotal(subtotal)
                .couponDiscount(couponDiscount)
                .shippingCost(shippingCost)
                .taxAmount(taxAmount)
                .total(totalAmount)
                .build();

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * ================================================================
         */
        // Omitted: Method is read-only lookup query block.

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        // Omitted: No processing metrics require structural masking vectors.

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return CartResponseDto.builder()
                .cartId(cart.getPkCartId())
                .status(cart.getFkCartStatus().getStatusCode())
                .createdAt(cart.getCreatedAtUtc())
                .expiresAt(cart.getExpiresAtUtc())
                .items(itemDtos)
                .totals(totalsDto)
                .lastUpdated(cart.getUpdatedAtUtc())
                .build();
    }
}