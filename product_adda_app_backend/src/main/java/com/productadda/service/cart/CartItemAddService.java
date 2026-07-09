package com.productadda.service.cart;

import com.productadda.dto.cart.CartItemAddRequestDto;
import com.productadda.dto.cart.CartItemAddResponseDto;

import com.productadda.entity.Cart;
import com.productadda.entity.CartItem;
import com.productadda.entity.CartStatus;
import com.productadda.entity.Inventory;
import com.productadda.entity.InventoryReservation;
import com.productadda.entity.Product;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.CartRepository;
import com.productadda.repository.CartItemRepository;
import com.productadda.repository.CartStatusRepository;
import com.productadda.repository.InventoryRepository;
import com.productadda.repository.InventoryReservationRepository;
import com.productadda.repository.ProductRepository;
import com.productadda.repository.UserRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class CartItemAddService {

        private static final String ACTIVE_CART_STATUS = "ACTIVE";

        private final CartRepository cartRepository;
        private final CartItemRepository cartItemRepository;
        private final CartStatusRepository cartStatusRepository;
        private final ProductRepository productRepository;
        private final InventoryRepository inventoryRepository;
        private final InventoryReservationRepository reservationRepository;
        private final UserRepository userRepository;

        private final UuidUtil uuidUtil;

        @Transactional
        public CartItemAddResponseDto addItemToCart(CartItemAddRequestDto requestDto) {
                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Strict multi-layer validation checking payload constraints,
                 * context, and dependencies.
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (requestDto == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Incoming request payload details must be provided");
                }

                if (requestDto.getProductId() == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Product ID identifier is required");
                }

                if (requestDto.getQuantity() == null || requestDto.getQuantity() <= 0) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
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

                Product product = productRepository.findById(requestDto.getProductId())
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.NOT_FOUND,
                                                "Product not found"));

                Inventory inventory = inventoryRepository.findByFkProduct(product)
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.NOT_FOUND,
                                                "Inventory track missing for target product"));

                if (inventory.getAvailableQuantity() < requestDto.getQuantity()) {
                        throw new ApiException(
                                        HttpStatus.CONFLICT,
                                        "Only " + inventory.getAvailableQuantity()
                                                        + " units available. Requested: " + requestDto.getQuantity());
                }

                boolean existsInCart = cartItemRepository
                                .findByFkCartAndFkProductAndIsActiveTrue(cart, product)
                                .isPresent();

                if (existsInCart) {
                        throw new ApiException(
                                        HttpStatus.CONFLICT,
                                        "This product is already in your cart. Use the update endpoint to change quantity.");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */

                // Unified Temporal Alignment applied explicitly prior to operations
                LocalDateTime baseTimestamp = LocalDateTime.now(ZoneOffset.UTC);

                CartItem item = new CartItem();

                item.setPkCartItemId(uuidUtil.generateUuidV7());
                item.setFkCart(cart);
                item.setFkProduct(product);
                item.setQuantity(requestDto.getQuantity());
                item.setPriceAtAdd(product.getPrice());
                item.setAddedAtUtc(baseTimestamp);
                item.setIsSavedForLater(false);
                item.setIsActive(true);

                InventoryReservation reservation = new InventoryReservation();

                reservation.setPkReservationId(uuidUtil.generateUuidV7());
                reservation.setFkProduct(product);
                reservation.setFkCart(cart);
                reservation.setFkCartItem(item);
                reservation.setReservedQuantity(requestDto.getQuantity());
                reservation.setIsActive(true);
                reservation.setExpiresAtUtc(baseTimestamp.plusHours(24));

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                CartItem savedItem = cartItemRepository.save(item);
                reservationRepository.save(reservation);

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * ================================================================
                 */

                // Omitted: No data items map to masked schemas.

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * ================================================================
                 */
                return CartItemAddResponseDto.builder()
                                .itemId(savedItem.getPkCartItemId())
                                .cartId(cart.getPkCartId())
                                .productId(product.getPkProductId())
                                .quantity(savedItem.getQuantity())
                                .priceAtAdd(savedItem.getPriceAtAdd())
                                .priceAlert(false)
                                .build();
        }
}