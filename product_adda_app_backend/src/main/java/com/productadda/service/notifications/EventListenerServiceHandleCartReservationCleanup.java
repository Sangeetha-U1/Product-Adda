package com.productadda.service.notifications;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.annotation.Propagation;

import com.productadda.entity.Cart;
import com.productadda.entity.InventoryReservation;

import com.productadda.event.cart.CartReservationCleanupEvent;

import com.productadda.repository.CartRepository;
import com.productadda.repository.InventoryReservationRepository;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * EventListenerServiceHandleCartReservationCleanup
 * Unlike the notification EventListenerServiceHandleXxx classes
 * (which are plain services invoked directly), this is a genuine
 * Spring-registered listener for CartReservationCleanupEvent.
 *
 * Fires as @TransactionalEventListener(AFTER_COMMIT): only runs once
 * the publishing transaction (CartClearService.clearCart) has
 * actually committed, so reservations are never released for a
 * clear that itself rolled back. Runs in its own new transaction
 * via @Transactional, since no transaction is active once
 * AFTER_COMMIT fires.
 *
 * Deliberately isolated from the cart-clearing transaction: a
 * failure here should not roll back the user's cart clear, and
 * each InventoryReservation already carries its own expiresAtUtc
 * as a natural fallback if this listener never succeeds.
 *
 * Idempotent by construction -- only rows still is_active=true are
 * touched, so re-running this handler for the same cart is always
 * safe.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class EventListenerServiceHandleCartReservationCleanup {

    private static final Logger log = LoggerFactory
            .getLogger(EventListenerServiceHandleCartReservationCleanup.class);

    private final CartRepository cartRepository;
    private final InventoryReservationRepository inventoryReservationRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCartReservationCleanup(CartReservationCleanupEvent event) {

        /*
         * Re-fetch a fresh, managed Cart instead of trusting a
         * reference carried on the event, since the original entity
         * is detached by the time AFTER_COMMIT fires.
         */
        Cart cart = cartRepository.findById(event.getCartId()).orElse(null);

        if (cart == null) {
            log.warn("CartReservationCleanupEvent received for missing cart id={}; skipping.", event.getCartId());
            return;
        }

        List<InventoryReservation> activeReservations = inventoryReservationRepository
                .findByFkCartAndIsActiveTrue(cart);

        for (InventoryReservation reservation : activeReservations) {
            reservation.setIsActive(false);
            inventoryReservationRepository.save(reservation);
        }
    }
}