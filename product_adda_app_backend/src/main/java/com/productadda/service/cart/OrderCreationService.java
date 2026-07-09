package com.productadda.service.cart;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.productadda.dto.cart.CartTotalsDto;

import com.productadda.entity.Address;
import com.productadda.entity.Cart;
import com.productadda.entity.CartItem;
import com.productadda.entity.Coupon;
import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;
import com.productadda.entity.OrderStatus;
import com.productadda.entity.User;
import com.productadda.entity.ItemStatus;

import com.productadda.exception.ApiException;

import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.OrderRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderCreationService {

        private static final DateTimeFormatter ORDER_NUMBER_TIMESTAMP_FORMAT = DateTimeFormatter
                        .ofPattern("yyyyMMddHHmmss");

        private final OrderRepository orderRepository;
        private final OrderItemRepository orderItemRepository;

        private final UuidUtil uuidUtil;

        public Order createOrder(
                        User user,
                        Cart cart,
                        List<CartItem> cartItems,
                        Address address,
                        Coupon coupon,
                        OrderStatus pendingStatus,
                        ItemStatus itemPendingStatus,
                        CartTotalsDto totals,
                        UUID idempotencyKey) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Isolated defensive re-check of caller-supplied
                 * entities. CheckoutService already validated these upstream, but
                 * this service must not assume that per the Zero-Trust standard.
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (user == null || cart == null || cartItems == null || cartItems.isEmpty()
                                || address == null || pendingStatus == null || totals == null || itemPendingStatus == null
                                || idempotencyKey == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Incomplete data supplied for order creation");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                // Not applicable: caller (CheckoutService) already authenticated the request.

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                // Not applicable: all referenced entities are passed in already-loaded.

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                LocalDateTime nowUtc = LocalDateTime.now(java.time.ZoneOffset.UTC);

                String orderNumber = "ORD-" + nowUtc.format(ORDER_NUMBER_TIMESTAMP_FORMAT) + "-"
                                + uuidUtil.generateUuidV7String().replace("-", "").substring(0, 6).toUpperCase();

                Order order = Order.builder()
                                .pkOrderId(uuidUtil.generateUuidV7())
                                .fkUser(user)
                                .fkCart(cart)
                                .fkStatus(pendingStatus)
                                .fkAddress(address)
                                .fkCoupon(coupon)
                                .orderNumber(orderNumber)
                                .subtotal(totals.getSubtotal())
                                .couponDiscount(totals.getCouponDiscount())
                                .shippingCost(totals.getShippingCost())
                                .taxAmount(totals.getTaxAmount())
                                .idempotencyKey(idempotencyKey)
                                .totalAmount(totals.getTotal())
                                .isActive(true)
                                .build();

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                Order savedOrder = orderRepository.save(order);

                for (CartItem cartItem : cartItems) {

                        OrderItem orderItem = OrderItem.builder()
                                        .pkOrderItemId(uuidUtil.generateUuidV7())
                                        .fkOrder(savedOrder)
                                        .fkProduct(cartItem.getFkProduct())
                                        .fkVendor(cartItem.getFkProduct().getFkVendor())
                                        .fkItemStatus(itemPendingStatus)
                                        .productNameSnapshot(cartItem.getFkProduct().getTitle())
                                        .quantity(cartItem.getQuantity())
                                        .unitPrice(cartItem.getPriceAtAdd())
                                        .lineTotal(cartItem.getPriceAtAdd()
                                                        .multiply(java.math.BigDecimal.valueOf(cartItem.getQuantity())))
                                        .isActive(true)
                                        .build();

                        orderItemRepository.save(orderItem);
                }

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * Description: created_at_utc/updated_at_utc are DB-generated
                 * (insertable = false), so the persisted entity is reloaded here
                 * to guarantee the caller receives accurate audit timestamps.
                 * ================================================================
                 */
                Order reloadedOrder = orderRepository.findById(savedOrder.getPkOrderId())
                                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Order persisted but could not be reloaded"));

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * Reason: This service returns the persisted entity itself; DTO
                 * mapping is the responsibility of the calling orchestrator
                 * (CheckoutService), which owns the response contract.
                 * ================================================================
                 */
                return reloadedOrder;
        }
}
