package com.productadda.worker.cart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.productadda.service.cart.CartExpirationService;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * CartExpirationWorker
 * Follows the same pattern as QueueProcessorWorker: a thin
 * @Component wrapping a try/catch around the actual service call,
 * so an exception in one run can never kill the scheduler thread
 * pool. Requires @EnableScheduling, already provided by
 * config/SchedulingConfig.java -- no change needed there.
 * ================================================================
 */
@Component
@RequiredArgsConstructor
public class CartExpirationWorker {

    private static final Logger log = LoggerFactory.getLogger(CartExpirationWorker.class);

    private final CartExpirationService cartExpirationService;

    /*
     * Runs every 24 hours (configurable via application.properties).
     * fixedDelay (not fixedRate) is used deliberately -- the next run
     * only starts after the previous run finishes, so executions never
     * overlap.
     */
    @Scheduled(fixedDelayString = "${productadda.cart.expiration.worker.fixed-delay-ms}")
    public void runCartExpiration() {
        try {
            cartExpirationService.processExpiredCarts();
        } catch (Exception exception) {
            log.error("Cart expiration run failed: {}", exception.getMessage(), exception);
        }
    }
}