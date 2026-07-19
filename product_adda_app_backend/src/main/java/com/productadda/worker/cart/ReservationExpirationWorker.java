package com.productadda.worker.cart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.productadda.service.cart.ReservationExpirationService;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * ReservationExpirationWorker
 * Safety net for InventoryReservation rows whose expiresAtUtc has
 * passed but were never deactivated by the normal cart-clear flow
 * (e.g. a process crash between the AAFTER_COMMIT publish and the
 * CartReservationCleanupEvent listener running). Follows the same
 * thin-wrapper pattern as QueueProcessorWorker and
 * CartExpirationWorker.
 * ================================================================
 */
@Component
@RequiredArgsConstructor
public class ReservationExpirationWorker {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpirationWorker.class);

    private final ReservationExpirationService reservationExpirationService;

    /*
     * Runs every 15 minutes, same cadence as CartExpirationWorker.
     */
    @Scheduled(fixedDelay = 900000)
    public void runReservationExpiration() {
        try {
            reservationExpirationService.processExpiredReservations();
        } catch (Exception exception) {
            log.error("Reservation expiration run failed: {}", exception.getMessage(), exception);
        }
    }
}