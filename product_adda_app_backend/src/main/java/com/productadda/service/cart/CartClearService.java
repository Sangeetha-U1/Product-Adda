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

import com.productadda.entity.Cart;
import com.productadda.entity.CartItem;
import com.productadda.entity.InventoryReservation;
import com.productadda.entity.User;

import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartClearService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void clearCart(UUID cartId) {
        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        // Confirm baseline structural state references
        if (cartId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cart ID cannot be null");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        // Inspect valid thread domain permissions programmatically
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User authentication context missing");
        }
        String currentUsername = authentication.getName();

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        // Retrieve the corresponding structural user footprint via parsed security data
        // context
        User user = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User entity mapping missing"));
        UUID currentUserId = user.getPkUserId();

        // Extract targeted reference instance parameters
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cart not found"));

        // Match safety boundaries with ownership profile records
        if (cart.getFkUser() == null || !cart.getFkUser().getPkUserId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: You do not own this cart");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        // Linear Step 1: Gather active container contents for processing execution path
        // metrics
        List<CartItem> structuralItems = cartItemRepository.findByFkCartAndIsActiveTrue(cart);

        // Linear Step 2: Symmetrically iterate loop to hard delete inventory
        // allocations tied to items in this cart
        for (CartItem item : structuralItems) {
            List<InventoryReservation> activeReservations = inventoryReservationRepository
                    .findByFkProductAndIsActiveTrue(item.getFkProduct());

            for (InventoryReservation res : activeReservations) {
                if (res.getFkCart() != null && res.getFkCart().getPkCartId().equals(cartId)) {
                    inventoryReservationRepository.delete(res);
                }
            }
        }

        // Linear Step 3: Permanently wipe matching item records via a hard repository
        // clear loop
        for (CartItem item : structuralItems) {
            cartItemRepository.delete(item);
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        cartRepository.save(cart);

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        // Omitted: logic rules require zero structural scrubbing

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        // Void process control sequence returns safely to standard endpoint thread
        // context
    }
}