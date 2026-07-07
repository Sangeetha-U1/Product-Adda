package com.productadda.service.cart;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.entity.Cart;
import com.productadda.entity.CartItem;
import com.productadda.entity.CartStatus;
import com.productadda.entity.InventoryReservation;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.CartItemRepository;
import com.productadda.repository.CartRepository;
import com.productadda.repository.CartStatusRepository;
import com.productadda.repository.InventoryReservationRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

// TODO: Replace direct InventoryReservation delete with event publishing (CartReservationCleanupEvent)
// TODO: Publish CartReservationCleanupEvent instead of deleting inventory reservations directly

@Service
@RequiredArgsConstructor
public class CartClearService {

    private static final String ACTIVE_CART_STATUS = "ACTIVE";

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final UserRepository userRepository;
    private final CartStatusRepository cartStatusRepository;

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

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */

        List<CartItem> structuralItems = cartItemRepository.findByFkCartAndIsActiveTrue(cart);

        List<InventoryReservation> cartReservations = inventoryReservationRepository
                .findByFkCartAndIsActiveTrue(cart);

        for (InventoryReservation reservation : cartReservations) {
            reservation.setIsActive(false);
            inventoryReservationRepository.save(reservation);
        }

        for (CartItem item : structuralItems) {
            item.setIsActive(false);
            cartItemRepository.save(item);
        }

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