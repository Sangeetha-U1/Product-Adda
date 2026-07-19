package com.productadda.service.cart;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.productadda.entity.Cart;

import com.productadda.repository.CartRepository;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * CartExpirationService
 * Description: Finds carts whose expiresAtUtc has passed and clears
 * their items, reservations, and coupon by delegating to
 * CartClearService.clearCart(Cart) -- the same clearing logic
 * used by the authenticated DELETE /api/carts endpoint. Reuses
 * rather than duplicates that logic, per the original TO DO.
 *
 * Deliberately NOT @Transactional at this level: clearCart(Cart) is
 * already @Transactional on CartClearService, and is called here as
 * an external bean call, so each cart's clear opens its own
 * independent transaction. One cart failing never rolls back or
 * blocks any other cart already cleared in this batch.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class CartExpirationService {

    private static final Logger log = LoggerFactory.getLogger(CartExpirationService.class);

    private final CartRepository cartRepository;
    private final CartClearService cartClearService;

    public void processExpiredCarts() {

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        List<Cart> expiredCarts = cartRepository.findByIsActiveTrueAndExpiresAtUtcBefore(now);

        int clearedCount = 0;
        int failedCount = 0;

        for (Cart cart : expiredCarts) {
            try {
                cartClearService.clearCart(cart);
                clearedCount++;
            } catch (Exception clearException) {
                failedCount++;
                log.warn("Failed to clear expired cart: cartId={}, reason={}",
                        cart.getPkCartId(), clearException.getMessage());
            }
        }

        log.info("Expired cart cleanup batch processed: found={}, cleared={}, failed={}",
                expiredCarts.size(), clearedCount, failedCount);
    }
}