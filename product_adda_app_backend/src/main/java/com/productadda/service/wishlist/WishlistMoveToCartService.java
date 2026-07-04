package com.productadda.service.wishlist;

import com.productadda.entity.User;
import com.productadda.entity.WishlistItem;
import com.productadda.entity.Product;
import com.productadda.entity.Cart;
import com.productadda.entity.CartItem;
import com.productadda.entity.InventoryReservation;

import com.productadda.exception.ApiException;

import com.productadda.repository.UserRepository;
import com.productadda.repository.WishlistItemRepository;
import com.productadda.repository.CartRepository;
import com.productadda.repository.CartItemRepository;
import com.productadda.repository.InventoryReservationRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistMoveToCartService {

        private final UuidUtil uuidUtil;
        private final UserRepository userRepository;
        private final WishlistItemRepository wishlistItemRepository;
        private final CartRepository cartRepository;
        private final CartItemRepository cartItemRepository;
        private final InventoryReservationRepository inventoryReservationRepository;

        @Transactional
        public void moveItemToCart(UUID wishlistItemId) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Enforces strict zero-trust parameters, ownership context
                 * security, and multi-layered system states.
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (wishlistItemId == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Wishlist item reference identifier cannot be null");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED,
                                        "Security context validation failed. Missing authentication token");
                }

                String principalName = authentication.getName();
                if (principalName == null || principalName.trim().isEmpty()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal extraction");
                }

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                User authenticatedUser = userRepository.findByEmail(principalName)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user account record could not be located"));

                if (!authenticatedUser.getIsActive()) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Access denied. User account context is currently deactivated");
                }

                WishlistItem targetWishlistItem = wishlistItemRepository.findById(wishlistItemId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Wishlist item record not found"));

                if (targetWishlistItem.getIsDeleted() || !targetWishlistItem.getIsActive()) {
                        throw new ApiException(HttpStatus.NOT_FOUND, "Wishlist item record not found");
                }

                User itemOwnerRecord = targetWishlistItem.getFkWishlist().getFkUser();
                if (!itemOwnerRecord.getPkUserId().equals(authenticatedUser.getPkUserId())) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Access authorization denied. Cross-user modification blocked");
                }

                Product matchingProductEntity = targetWishlistItem.getFkProduct();

                // Enforce explicit initialization structure alignment by changing default
                // generation to an explicit error throw
                Cart operationalCart = cartRepository
                                .findByFkUserAndFkCartStatus_StatusCodeAndIsActiveTrue(authenticatedUser, "ACTIVE")
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Active cart container tracking session has not been initialized"));

                // Pulling through the valid active filter query method matching your
                // CartItemRepository interface signature
                List<CartItem> structuralCartItemsList = cartItemRepository
                                .findByFkCartAndIsActiveTrue(operationalCart);
                boolean isProductAlreadyInCart = structuralCartItemsList.stream()
                                .anyMatch(ci -> ci.getFkProduct().getPkProductId()
                                                .equals(matchingProductEntity.getPkProductId()));

                if (isProductAlreadyInCart) {
                        throw new ApiException(HttpStatus.CONFLICT, "This product is already in your cart");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                LocalDateTime atomicExecutionUtcTimestamp = LocalDateTime.now(ZoneOffset.UTC);

                // Map all mandatory entity fields to resolve the non-null database layout
                // constraints perfectly
                CartItem orchestratedCartItem = CartItem.builder()
                                .pkCartItemId(uuidUtil.generateUuidV7())
                                .fkCart(operationalCart)
                                .fkProduct(matchingProductEntity)
                                .quantity(1)
                                .priceAtAdd(matchingProductEntity.getPrice())
                                .addedAtUtc(atomicExecutionUtcTimestamp)
                                .isSavedForLater(false)
                                .isActive(true)
                                .build();

                // Entity references saved to satisfy subsequent mapping joins natively
                CartItem savedCartItem = cartItemRepository.save(orchestratedCartItem);

                // Mapped attributes updated to mirror reservedQuantity and fkCartItem from
                // entity specs
                InventoryReservation engineeredReservation = InventoryReservation.builder()
                                .pkReservationId(uuidUtil.generateUuidV7())
                                .fkProduct(matchingProductEntity)
                                .fkCart(operationalCart)
                                .fkCartItem(savedCartItem)
                                .reservedQuantity(1)
                                .expiresAtUtc(atomicExecutionUtcTimestamp.plusMinutes(60))
                                .isActive(true)
                                .build();

                targetWishlistItem.setIsDeleted(true);
                operationalCart.setUpdatedAtUtc(atomicExecutionUtcTimestamp);

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                inventoryReservationRepository.save(engineeredReservation);
                wishlistItemRepository.save(targetWishlistItem);
                cartRepository.save(operationalCart);

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * Description: Terminal transactional mapping void of structural masking
                 * targets.
                 * ================================================================
                 */

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * Description: Returns void via architectural blueprint constraints.
                 * ================================================================
                 */
        }
}