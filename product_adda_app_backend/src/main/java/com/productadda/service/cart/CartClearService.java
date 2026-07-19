package com.productadda.service.cart;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.entity.Cart;
import com.productadda.entity.CartItem;
import com.productadda.entity.CartStatus;
import com.productadda.entity.User;

import com.productadda.event.cart.CartReservationCleanupEvent;

import com.productadda.exception.ApiException;

import com.productadda.repository.CartItemRepository;
import com.productadda.repository.CartRepository;
import com.productadda.repository.CartStatusRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartClearService {

    private static final String ACTIVE_CART_STATUS = "ACTIVE";

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final CartStatusRepository cartStatusRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void clearCart() {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: Validate authenticated user and required database
         * entities before clearing the active cart.
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================

        // Public request body validation is not applicable because this
        // operation accepts no request payload.

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User authentication context missing");
        }

        String currentUsername = authentication.getName();

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================

        User user = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "User entity mapping missing"));

        CartStatus activeStatus = cartStatusRepository.findByStatusCode(ACTIVE_CART_STATUS)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Lookup cart status 'ACTIVE' not found"));

        Cart cart = cartRepository.findByFkUserAndFkCartStatusAndIsActiveTrue(
                        user,
                        activeStatus)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "No active cart found for the authenticated user."));

        clearCart(cart);
    }

    /*
     * ================================================================
     * CLEAR CART (cart-scoped overload)
     * Description: Contains the actual clearing workflow, decoupled
     * from any HTTP/authentication context, so it can be reused by
     * both the authenticated clearCart() entry point above and the
     * scheduled expired-cart job, which resolves carts for many
     * different users with no SecurityContext available.
     * ================================================================
     */
    @Transactional
    public void clearCart(Cart cart) {

        if (cart == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A cart is required to perform clearing");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */

        List<CartItem> structuralItems = cartItemRepository.findByFkCartAndIsActiveTrue(cart);

        for (CartItem item : structuralItems) {
            item.setIsActive(false);
            cartItemRepository.save(item);
        }

        /*
         * Publish for decoupled, idempotent InventoryReservation
         * cleanup instead of deactivating reservations here directly
         * see EventListenerServiceHandleCartReservationCleanup.
         */
        applicationEventPublisher.publishEvent(new CartReservationCleanupEvent(cart.getPkCartId()));

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */

        // Create one synchronized UTC timestamp for this persistence sequence.
        LocalDateTime currentUtcDateTime = LocalDateTime.now(ZoneOffset.UTC);

        cart.setFkCoupon(null);
        cart.setUpdatedAtUtc(currentUtcDateTime);

        cartRepository.save(cart);

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */

        // No response payload exists for this workflow.
        // No post-persistence sanitization is required.

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */

        // No response DTO is required because this operation returns void.
    }
}