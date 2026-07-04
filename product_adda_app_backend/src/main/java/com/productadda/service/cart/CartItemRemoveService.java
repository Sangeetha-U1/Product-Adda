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

import com.productadda.dto.cart.CartResponseDto;
import com.productadda.dto.cart.CartItemResponseDto;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartItemRemoveService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartResponseDto removeItem(UUID cartId, UUID itemId) {
        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        // Check input method parameters for missing structural metrics
        if (cartId == null || itemId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Identifiers cannot be null");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        // Intercept user domain metrics securely from active runtime thread block
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User authentication context missing");
        }
        String currentUsername = authentication.getName();

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        // Parse database reference schema using email string identities safely
        User user = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User entity mapping missing"));
        UUID currentUserId = user.getPkUserId();

        // Verify active parent container record visibility mappings
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cart not found"));

        // Match structural boundaries against client profile configuration mappings
        if (cart.getFkUser() == null || !cart.getFkUser().getPkUserId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: You do not own this cart");
        }

        // Locate targeted database row entries cleanly
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found in cart"));

        // Guard horizontal domain traversal parameters safely
        if (cartItem.getFkCart() == null || !cartItem.getFkCart().getPkCartId().equals(cartId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Item does not belong to the specified cart");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        // Linear Step 1: Locate all valid allocation tracking chains matching the
        // product reference context
        List<InventoryReservation> linkedReservations = inventoryReservationRepository
                .findByFkProductAndIsActiveTrue(cartItem.getFkProduct());

        // Linear Step 2: Hard delete the assigned allocations matched within this
        // specific cart structure framework
        for (InventoryReservation res : linkedReservations) {
            if (res.getFkCart() != null && res.getFkCart().getPkCartId().equals(cartId)) {
                inventoryReservationRepository.delete(res);
            }
        }

        // Linear Step 3: Hard delete the specific target cart item entity permanently
        // from the database table rows
        cartItemRepository.delete(cartItem);

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
        // Extract remaining active structural items following execution path deletions
        List<CartItem> structuralItems = cartItemRepository.findByFkCartAndIsActiveTrue(cart);

        // Initialize target response list item collection container
        List<CartItemResponseDto> sanitizedItems = new ArrayList<>();

        // Sequentially map remaining structural items into sanitized nested DTO records
        // without inline chaining
        for (CartItem item : structuralItems) {
            UUID mappedProductId = null;
            if (item.getFkProduct() != null) {
                mappedProductId = item.getFkProduct().getPkProductId();
            }

            CartItemResponseDto childDto = CartItemResponseDto.builder()
                    .itemId(item.getPkCartItemId())
                    .productId(mappedProductId)
                    .quantity(item.getQuantity())
                    .priceAtAdd(item.getPriceAtAdd())
                    .build();

            sanitizedItems.add(childDto);
        }

        // Safe status string resolution from related structural configurations
        String resolvedStatus = null;
        if (cart.getFkCartStatus() != null) {
            resolvedStatus = cart.getFkCartStatus().getStatusCode();
        }

        // Completely compose response blueprint architecture inside Section 4 boundary
        CartResponseDto outputPayload = CartResponseDto.builder()
                .cartId(cart.getPkCartId())
                .status(resolvedStatus)
                .expiresAt(cart.getExpiresAtUtc())
                .items(sanitizedItems)
                .build();

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        // Return pre-constructed response object mapping cleanly
        return outputPayload;
    }
}