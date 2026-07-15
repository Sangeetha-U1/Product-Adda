package com.productadda.service.notifications;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.productadda.entity.Notification;

import com.productadda.exception.ApiException;

@Service
public class NotificationDispatchServiceSms {

    /*
     * ================================================================
     * DISPATCH SMS
     * Description: Structural placeholder for future Twilio
     * integration, per Dheeraj's Day 1 decision -- the code path exists
     * so provider wiring can be dropped in later without touching
     * NotificationQueueProcessorService, but it does not actually send
     * anything today: no SMS provider credentials/setup exist yet.
     * No SMS `notifications` rows are created on Day 1 (see
     * NotificationCreationService.DAY_ONE_CHANNEL_NAMES), so this
     * method is currently unreachable in production; it exists to be
     * exercised directly by Postman/unit testing only, to prove the
     * failure path behaves correctly once wired up.
     * TODO: Replace this thrown exception with a real Twilio API call
     * once provider credentials are configured (credentials belong in
     * application.properties/env vars, not the DB -- see
     * WEEK_8_EXECUTION_PLAN_REVISED.md, item 4).
     * ================================================================
     */
    public void dispatchSms(Notification notification) {
        throw new ApiException(HttpStatus.NOT_IMPLEMENTED,
                "SMS provider is not configured yet");
    }
}
