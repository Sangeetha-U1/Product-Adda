package com.productadda.service.cart;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import com.productadda.exception.ApiException;

import com.productadda.repository.CartRepository;
import com.productadda.repository.CartItemRepository;
import com.productadda.repository.InventoryReservationRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.CartStatusRepository;

import com.productadda.entity.Cart;
import com.productadda.entity.CartItem;
import com.productadda.entity.CartStatus;
import com.productadda.entity.InventoryReservation;
import com.productadda.entity.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

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
         * ================================================================
         */

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User authentication context missing");
        }

        String currentUsername = authentication.getName();

        User user = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User entity mapping missing"));

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

        for (CartItem item : structuralItems) {
            List<InventoryReservation> activeReservations = inventoryReservationRepository
                    .findByFkProductAndIsActiveTrue(item.getFkProduct());

            for (InventoryReservation res : activeReservations) {
                if (res.getFkCart() != null &&
                        res.getFkCart().getPkCartId().equals(cart.getPkCartId())) {
                    inventoryReservationRepository.delete(res);
                }
            }
        }

        for (CartItem item : structuralItems) {
            cartItemRepository.delete(item);
        }

        /*
         * ================================================================
         * 3. CART STATE RESET (IMPORTANT FOR PRICING ENGINE CONSISTENCY)
         * ================================================================
         */
        cart.setFkCoupon(null);
        cart.setUpdatedAtUtc(LocalDateTime.now(ZoneOffset.UTC));

        cartRepository.save(cart);

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        // No response payload required
    }
}