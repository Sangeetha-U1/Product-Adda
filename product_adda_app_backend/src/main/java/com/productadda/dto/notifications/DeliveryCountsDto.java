package com.productadda.dto.notifications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * Semantic mapping, since this system has no true delivery-confirmation
 * signal (see NotificationHistoryDto):
 * - sent      = every attempted dispatch that reached a terminal outcome
 *               (SENT + FAILED + MAX_RETRIES_FAILED)
 * - delivered = successfully dispatched (dispatch_status = SENT) --
 *               treated as the delivery proxy
 * - failed    = FAILED + MAX_RETRIES_FAILED
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryCountsDto {

    private long sent;

    private long delivered;

    private long failed;
}
