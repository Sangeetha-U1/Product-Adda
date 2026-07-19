package com.productadda.service.cart;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.entity.InventoryReservation;

import com.productadda.repository.InventoryReservationRepository;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * ReservationExpirationService
 * Description: Deactivates InventoryReservation rows whose own
 * expiresAtUtc has passed, independent of the cart they belong to.
 * This exists because Cart.expiresAtUtc (90 days) and
 * InventoryReservation.expiresAtUtc (24 hours) run on very
 * different timescales -- CartExpirationService alone would leave
 * an orphaned reservation live for up to 90 days on an otherwise
 * still-active cart. This job closes that gap directly.
 *
 * Whole method is @Transactional, matching
 * NotificationQueueProcessorService.processPendingBatch(): a single
 * batch transaction, with each row's failure caught and logged
 * individually so one bad row never halts the rest of the batch.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class ReservationExpirationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpirationService.class);

    private final InventoryReservationRepository inventoryReservationRepository;

    @Transactional
    public void processExpiredReservations() {

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        List<InventoryReservation> expiredReservations = inventoryReservationRepository
                .findByIsActiveTrueAndExpiresAtUtcBefore(now);

        int deactivatedCount = 0;
        int failedCount = 0;

        for (InventoryReservation reservation : expiredReservations) {
            try {
                reservation.setIsActive(false);
                inventoryReservationRepository.save(reservation);
                deactivatedCount++;
            } catch (Exception saveException) {
                failedCount++;
                log.warn("Failed to deactivate expired reservation: reservationId={}, reason={}",
                        reservation.getPkReservationId(), saveException.getMessage());
            }
        }

        log.info("Expired reservation cleanup batch processed: found={}, deactivated={}, failed={}",
                expiredReservations.size(), deactivatedCount, failedCount);
    }
}