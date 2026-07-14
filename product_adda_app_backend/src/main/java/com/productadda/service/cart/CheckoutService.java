package com.productadda.service.cart;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.cart.CartTotalsDto;
import com.productadda.dto.cart.CheckoutRequestDto;
import com.productadda.dto.cart.CheckoutResponseDto;
import com.productadda.dto.cart.CouponApplyRequestDto;
import com.productadda.dto.cart.CouponValidationResponseDto;
import com.productadda.dto.order.OrderItemSummaryDto;

import com.productadda.entity.Address;
import com.productadda.entity.Cart;
import com.productadda.entity.CartItem;
import com.productadda.entity.CartStatus;
import com.productadda.entity.Coupon;
import com.productadda.entity.CouponUsageHistory;
import com.productadda.entity.Inventory;
import com.productadda.entity.InventoryReservation;
import com.productadda.entity.Order;
import com.productadda.entity.OrderStatus;
import com.productadda.entity.Product;
import com.productadda.entity.User;
import com.productadda.entity.ItemStatus;

import com.productadda.exception.ApiException;

import com.productadda.repository.AddressRepository;
import com.productadda.repository.CartItemRepository;
import com.productadda.repository.CartRepository;
import com.productadda.repository.CartStatusRepository;
import com.productadda.repository.CouponUsageHistoryRepository;
import com.productadda.repository.InventoryRepository;
import com.productadda.repository.InventoryReservationRepository;
import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.OrderStatusRepository;
import com.productadda.repository.ProductRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.ItemStatusRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

// TODO: Call payment gateway (Stripe/Razorpay) integration as a separate,
// explicit client call to POST /api/payment/create after checkout succeeds.
// Checkout intentionally does not create a Payment record (see Day 5 decision).

@Service
@RequiredArgsConstructor
public class CheckoutService {

        private static final String ACTIVE_CART_STATUS = "ACTIVE";
        private static final String COMPLETED_CART_STATUS = "COMPLETED";
        private static final String PENDING_ORDER_STATUS = "PENDING";
        private static final String PENDING_ITEM_STATUS = "PENDING";

        private final UserRepository userRepository;
        private final CartRepository cartRepository;
        private final CartItemRepository cartItemRepository;
        private final CartStatusRepository cartStatusRepository;
        private final AddressRepository addressRepository;
        private final ProductRepository productRepository;
        private final InventoryRepository inventoryRepository;
        private final InventoryReservationRepository inventoryReservationRepository;
        private final CouponValidationService couponValidationService;
        private final CouponUsageHistoryRepository couponUsageHistoryRepository;
        private final OrderRepository orderRepository;
        private final OrderItemRepository orderItemRepository;
        private final OrderStatusRepository orderStatusRepository;
        private final ItemStatusRepository itemStatusRepository;

        private final CheckoutPricingService checkoutPricingService;
        private final OrderCreationService orderCreationService;
        private final CartInitializeService cartInitializeService;

        private final UuidUtil uuidUtil;

        @Transactional
        public CheckoutResponseDto checkout(CheckoutRequestDto requestDto) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Full, self-contained, zero-trust validation. This
                 * service never assumes /checkout/validate was called beforehand.
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (requestDto == null || requestDto.getAddressId() == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Shipping address id is required");
                }

                if (requestDto.getIdempotencyKey() == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Idempotency-Key is required");
                }

                // Note: the frontend must generate the UUID before calling checkout
                // and send the same UUID if the user retries the same checkout. The backend
                // remains the source of truth for idempotency.

                UUID idempotencyKey = requestDto.getIdempotencyKey();

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
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user no longer exists"));

                // Idempotency short-circuit: replay an already-created order untouched.
                Order existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
                if (existingOrder != null) {

                        if (!existingOrder.getFkUser().getPkUserId().equals(user.getPkUserId())) {
                                throw new ApiException(HttpStatus.FORBIDDEN,
                                                "Idempotency key does not belong to this user");
                        }

                        List<OrderItemSummaryDto> existingItemSummaries = orderItemRepository
                                        .findByFkOrderAndIsActiveTrue(existingOrder)
                                        .stream()
                                        .map(item -> OrderItemSummaryDto.builder()
                                                        .orderItemId(item.getPkOrderItemId())
                                                        .productId(item.getFkProduct() != null
                                                                        ? item.getFkProduct().getPkProductId()
                                                                        : null)
                                                        .productNameSnapshot(item.getProductNameSnapshot())
                                                        .quantity(item.getQuantity())
                                                        .unitPrice(item.getUnitPrice())
                                                        .lineTotal(item.getLineTotal())
                                                        .build())
                                        .collect(Collectors.toList());

                        return CheckoutResponseDto.builder()
                                        .orderId(existingOrder.getPkOrderId())
                                        .orderNumber(existingOrder.getOrderNumber())
                                        .statusName(existingOrder.getFkStatus().getStatusName())
                                        .subtotal(existingOrder.getSubtotal())
                                        .couponDiscount(existingOrder.getCouponDiscount())
                                        .shippingCost(existingOrder.getShippingCost())
                                        .taxAmount(existingOrder.getTaxAmount())
                                        .totalAmount(existingOrder.getTotalAmount())
                                        .idempotencyKey(existingOrder.getIdempotencyKey())
                                        .createdAtUtc(existingOrder.getCreatedAtUtc())
                                        .items(existingItemSummaries)
                                        .build();
                }

                CartStatus activeStatus = cartStatusRepository.findByStatusCode(ACTIVE_CART_STATUS)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Lookup cart status 'ACTIVE' not found"));

                Cart cart = cartRepository.findByFkUserAndFkCartStatusAndIsActiveTrue(user, activeStatus)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "No active cart found for the authenticated user."));

                List<CartItem> cartItems = cartItemRepository.findByFkCartAndIsActiveTrue(cart);

                if (cartItems.isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Cart is empty.");
                }

                Address address = addressRepository.findById(requestDto.getAddressId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Shipping address not found"));

                if (!address.getFkUser().getPkUserId().equals(user.getPkUserId())) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Address does not belong to the authenticated user");
                }

                if (!Boolean.TRUE.equals(address.getIsActive())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Selected shipping address is no longer active");
                }

                Coupon coupon = cart.getFkCoupon();

                if (coupon != null) {
                        CouponValidationResponseDto couponValidation = couponValidationService.validateCoupon(
                                        CouponApplyRequestDto.builder().couponCode(coupon.getCouponCode()).build());

                        if (!Boolean.TRUE.equals(couponValidation.getValid())) {
                                throw new ApiException(HttpStatus.CONFLICT,
                                                "Applied coupon is no longer valid: " + couponValidation.getReason());
                        }
                }

                // Stock check re-verified independently (Zero-Trust: never assumes /validate
                // ran first).
                for (CartItem item : cartItems) {
                        Product product = productRepository.findById(item.getFkProduct().getPkProductId())
                                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));

                        Inventory inventory = inventoryRepository.findByFkProduct(product)
                                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                        "Inventory track missing for target product"));

                        if (inventory.getAvailableQuantity() < item.getQuantity()) {
                                throw new ApiException(HttpStatus.CONFLICT,
                                                "Item " + product.getTitle() + " has insufficient stock. Only "
                                                                + inventory.getAvailableQuantity() + " available.");
                        }
                }

                OrderStatus pendingStatus = orderStatusRepository.findByStatusName(PENDING_ORDER_STATUS)
                                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Order status 'PENDING' not found"));

                ItemStatus itemPendingStatus = itemStatusRepository.findByStatusName(PENDING_ITEM_STATUS)
                                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Item status 'PENDING' not found"));

                CartStatus completedStatus = cartStatusRepository.findByStatusCode(COMPLETED_CART_STATUS)
                                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Lookup cart status 'COMPLETED' not found"));

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                CartTotalsDto totals = checkoutPricingService.calculateCheckoutTotals(
                                cartItems, coupon, address, requestDto.getShippingMethodId());

                LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                Order savedOrder = orderCreationService.createOrder(
                                user, cart, cartItems, address, coupon, pendingStatus, itemPendingStatus, totals,
                                idempotencyKey);

                if (coupon != null) {
                        CouponUsageHistory usageHistory = CouponUsageHistory.builder()
                                        .pkCouponUsageId(uuidUtil.generateUuidV7())
                                        .fkCoupon(coupon)
                                        .fkUser(user)
                                        .fkCart(cart)
                                        .fkOrder(savedOrder)
                                        .discountAmount(totals.getCouponDiscount())
                                        .usedAtUtc(nowUtc)
                                        .isActive(true)
                                        .build();
                        couponUsageHistoryRepository.save(usageHistory);

                        coupon.setUsageCount(coupon.getUsageCount() + 1);
                }

                // Inventory conversion: permanently deduct ordered quantity from available
                // stock.
                // TODO: Clarify reserved vs sold inventory states (per Day 5 open decision).
                // reservedQuantity is intentionally left untouched for now.
                for (CartItem item : cartItems) {
                        Inventory inventory = inventoryRepository.findByFkProduct(item.getFkProduct())
                                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                        "Inventory track missing for target product"));

                        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - item.getQuantity());
                        inventoryRepository.save(inventory);
                }

                // Deactivate (soft) the add-to-cart inventory reservations now that stock has
                // been converted.
                List<InventoryReservation> activeReservations = inventoryReservationRepository
                                .findByFkCartAndIsActiveTrue(cart);
                for (InventoryReservation reservation : activeReservations) {
                        reservation.setIsActive(false);
                        inventoryReservationRepository.save(reservation);
                }

                cart.setFkCartStatus(completedStatus);

                cartRepository.save(cart);

                // Create fresh active cart for next shopping session
                cartInitializeService.initializeCart();

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * ================================================================
                 */
                List<OrderItemSummaryDto> itemSummaries = orderItemRepository.findByFkOrderAndIsActiveTrue(savedOrder)
                                .stream()
                                .map(item -> OrderItemSummaryDto.builder()
                                                .orderItemId(item.getPkOrderItemId())
                                                .productId(item.getFkProduct() != null
                                                                ? item.getFkProduct().getPkProductId()
                                                                : null)
                                                .productNameSnapshot(item.getProductNameSnapshot())
                                                .quantity(item.getQuantity())
                                                .unitPrice(item.getUnitPrice())
                                                .lineTotal(item.getLineTotal())
                                                .build())
                                .collect(Collectors.toList());

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * ================================================================
                 */
                return CheckoutResponseDto.builder()
                                .orderId(savedOrder.getPkOrderId())
                                .orderNumber(savedOrder.getOrderNumber())
                                .statusName(savedOrder.getFkStatus().getStatusName())
                                .subtotal(savedOrder.getSubtotal())
                                .couponDiscount(savedOrder.getCouponDiscount())
                                .shippingCost(savedOrder.getShippingCost())
                                .taxAmount(savedOrder.getTaxAmount())
                                .totalAmount(savedOrder.getTotalAmount())
                                .idempotencyKey(savedOrder.getIdempotencyKey())
                                .createdAtUtc(savedOrder.getCreatedAtUtc())
                                .items(itemSummaries)
                                .build();
        }
}
