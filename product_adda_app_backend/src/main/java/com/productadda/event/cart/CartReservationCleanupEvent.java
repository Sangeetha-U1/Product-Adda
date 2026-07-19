package com.productadda.event.cart;

import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * CartReservationCleanupEvent
 * Published by CartClearService after a cart's items have been
 * cleared, so InventoryReservation cleanup can happen decoupled
 * from the cart-clearing transaction. Carries only the cart ID
 * (not the Cart entity) because this event is consumed via a
 * @TransactionalEventListener(AFTER_COMMIT) -- by the time it runs,
 * the original Cart instance is a detached entity from a closed
 * persistence context, so the listener re-fetches a fresh, managed
 * Cart from the repository instead of reusing a stale reference.
 * ================================================================
 */
@Getter
@RequiredArgsConstructor
public class CartReservationCleanupEvent {

    private final UUID cartId;
}