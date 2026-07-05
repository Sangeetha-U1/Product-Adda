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
import com.productadda.repository.CartStatusRepository;

import com.productadda.entity.Cart;
import com.productadda.entity.CartItem;
import com.productadda.entity.CartStatus;
import com.productadda.entity.Inventory;
import com.productadda.entity.InventoryReservation;
import com.productadda.entity.User;

import com.productadda.dto.cart.CartResponseDto;
import com.productadda.dto.cart.CartItemResponseDto;
import com.productadda.dto.cart.CartTotalsDto;

import com.productadda.util.UuidUtil;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

// TODO: Replace direct InventoryReservation delete with event publishing (CartReservationCleanupEvent)
// TODO: Publish CartReservationCleanupEvent instead of deleting inventory reservations directly

@Service
@RequiredArgsConstructor
public class CartItemUpdateService {

    private static final String ACTIVE_CART_STATUS = "ACTIVE";

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final UserRepository userRepository;
    private final CartStatusRepository cartStatusRepository;
    private final CartPricingService cartPricingService;

    private final UuidUtil uuidUtil;

    @Transactional
    public CartResponseDto updateQuantity(UUID itemId, Integer newQuantity) {
        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        if (itemId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cart item ID is required");
        }

        if (newQuantity == null || newQuantity <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
        }

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

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found in cart"));

        if (cartItem.getFkCart() == null || !cartItem.getFkCart().getPkCartId().equals(cart.getPkCartId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Item does not belong to the specified cart");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */

        int oldQuantity = cartItem.getQuantity();
        int quantityDelta = newQuantity - oldQuantity;
        LocalDateTime temporalNow = LocalDateTime.now(ZoneOffset.UTC);

        Inventory inventory = inventoryRepository.findByFkProduct(cartItem.getFkProduct())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product inventory tracking record missing"));

        if (quantityDelta > 0 && inventory.getAvailableQuantity() < quantityDelta) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Only " + inventory.getAvailableQuantity() + " additional units available");
        }

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

        List<InventoryReservation> activeReservations = inventoryReservationRepository
                .findByFkProductAndIsActiveTrue(cartItem.getFkProduct());

        int releaseAmount = Math.abs(quantityDelta);

        for (InventoryReservation res : activeReservations) {
            if (quantityDelta >= 0 || releaseAmount <= 0) {
                continue;
            }
            if (res.getFkCart() != null && res.getFkCart().getPkCartId().equals(cart.getPkCartId())) {
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

        List<CartItem> structuralItems = cartItemRepository.findByFkCartAndIsActiveTrue(cart);
        List<CartItemResponseDto> sanitizedItems = new ArrayList<>();

        for (CartItem item : structuralItems) {
            UUID mappedProductId = null;
            if (item.getFkProduct() != null) {
                mappedProductId = item.getFkProduct().getPkProductId();
            }

            sanitizedItems.add(CartItemResponseDto.builder()
                    .itemId(item.getPkCartItemId())
                    .productId(mappedProductId)
                    .quantity(item.getQuantity())
                    .priceAtAdd(item.getPriceAtAdd())
                    .build());
        }

        String resolvedStatus = null;
        if (cart.getFkCartStatus() != null) {
            resolvedStatus = cart.getFkCartStatus().getStatusCode();
        }

        /*
         * ================================================================
         * 5. PRICING ENGINE INTEGRATION
         * ================================================================
         */
        CartTotalsDto totalsDto = cartPricingService.calculateTotals(structuralItems, cart);

        /*
         * ================================================================
         * 6. RESPONSE MAPPING
         * ================================================================
         */
        return CartResponseDto.builder()
                .cartId(cart.getPkCartId())
                .status(resolvedStatus)
                .expiresAt(cart.getExpiresAtUtc())
                .items(sanitizedItems)
                .totals(totalsDto)
                .lastUpdated(cart.getUpdatedAtUtc())
                .build();
    }
}