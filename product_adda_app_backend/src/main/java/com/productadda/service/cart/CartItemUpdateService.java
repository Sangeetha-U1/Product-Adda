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
import com.productadda.repository.InventoryRepository;
import com.productadda.repository.InventoryReservationRepository;
import com.productadda.repository.UserRepository;

import com.productadda.entity.Cart;
import com.productadda.entity.CartItem;
import com.productadda.entity.Inventory;
import com.productadda.entity.InventoryReservation;
import com.productadda.entity.User;

import com.productadda.dto.cart.CartResponseDto;
import com.productadda.dto.cart.CartItemResponseDto;

import com.productadda.util.UuidUtil;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class CartItemUpdateService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final UserRepository userRepository;
    private final UuidUtil uuidUtil;

    @Transactional
    public CartResponseDto updateQuantity(UUID cartId, UUID itemId, Integer newQuantity) {
        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        // Verify incoming method parameters are not null
        if (cartId == null || itemId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Identifiers cannot be null");
        }
        // Enforce boundary bounds rules on targeted update quantities
        if (newQuantity == null || newQuantity <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        // Pull context credentials from active execution context thread
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User authentication context missing");
        }
        String currentUsername = authentication.getName();

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        // Pull the user entity by email to retrieve the proper underlying surrogate
        // UUID primary key
        User user = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User entity mapping missing"));
        UUID currentUserId = user.getPkUserId();

        // Check structural existence paths of cart records
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cart not found"));

        // Match owner keys directly to safe authorization validation metrics
        if (cart.getFkUser() == null || !cart.getFkUser().getPkUserId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: You do not own this cart");
        }

        // Locate targeted items mapped to standard schemas
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found in cart"));

        // Confirm accurate intra-file configuration properties
        if (cartItem.getFkCart() == null || !cartItem.getFkCart().getPkCartId().equals(cartId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Item does not belong to the specified cart");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        // Establish initial values and current state variables
        int oldQuantity = cartItem.getQuantity();
        int quantityDelta = newQuantity - oldQuantity;
        LocalDateTime temporalNow = LocalDateTime.now(ZoneOffset.UTC);

        // Linear Step 1: Find entity tracker using clean product parameters from the
        // entity layer
        Inventory inventory = inventoryRepository.findByFkProduct(cartItem.getFkProduct())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product inventory tracking record missing"));

        // Linear Step 2: Validate stock ceilings if an increase is requested
        if (quantityDelta > 0 && inventory.getAvailableQuantity() < quantityDelta) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Only " + inventory.getAvailableQuantity() + " additional units available");
        }

        // Linear Step 3: Handle supplementary reservation block creation for positive
        // deltas
        if (quantityDelta > 0) {
            InventoryReservation supplementalReservation = InventoryReservation.builder()
                    .pkReservationId(uuidUtil.generateUuidV7())
                    .fkProduct(cartItem.getFkProduct())
                    .fkCart(cart)
                    .fkCartItem(cartItem)
                    .reservedQuantity(quantityDelta)
                    .expiresAtUtc(temporalNow.plusHours(24))
                    .isActive(true)
                    .build();
            inventoryReservationRepository.save(supplementalReservation);
        }

        // Linear Step 4: Extract active reservations matching this explicit context
        // path criteria for negative deltas
        List<InventoryReservation> activeReservations = inventoryReservationRepository
                .findByFkProductAndIsActiveTrue(cartItem.getFkProduct());

        // Linear Step 5: Define mutable tracking balance to track remaining release
        // quantity requirement
        int releaseAmount = Math.abs(quantityDelta);

        // Linear Step 6: Sequentially release quantities from existing active records
        // under a clean single loop layout
        for (InventoryReservation res : activeReservations) {
            if (quantityDelta >= 0 || releaseAmount <= 0) {
                continue;
            }
            if (res.getFkCart() != null && res.getFkCart().getPkCartId().equals(cartId)) {
                if (res.getReservedQuantity() <= releaseAmount) {
                    releaseAmount -= res.getReservedQuantity();
                    inventoryReservationRepository.delete(res);
                } else {
                    res.setReservedQuantity(res.getReservedQuantity() - releaseAmount);
                    releaseAmount = 0;
                    inventoryReservationRepository.save(res);
                }
            }
        }

        // Linear Step 7: Apply updated structural count properties directly to the
        // primary target model instance
        cartItem.setQuantity(newQuantity);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        cartItemRepository.save(cartItem);
        cartRepository.save(cart);

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        // Extract active structural items following state synchronization sequence
        List<CartItem> structuralItems = cartItemRepository.findByFkCartAndIsActiveTrue(cart);

        // Initialize target response list item collection container
        List<CartItemResponseDto> sanitizedItems = new ArrayList<>();

        // Sequentially map structural items into sanitized nested DTO records without
        // inline chaining
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