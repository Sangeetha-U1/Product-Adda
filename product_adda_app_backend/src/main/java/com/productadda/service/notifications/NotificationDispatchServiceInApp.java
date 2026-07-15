package com.productadda.service.notifications;

import org.springframework.stereotype.Service;

@Service
public class NotificationDispatchServiceInApp {

    /*
     * ================================================================
     * DISPATCH IN APP
     * Description: In-app notifications have no external provider --
     * the `notifications` row itself IS the delivered artifact (the
     * in-app notification center reads it directly, filtered to
     * fk_channel_id = IN_APP). This method is intentionally a no-op;
     * it exists so NotificationQueueProcessorService's per-channel
     * dispatch routing stays symmetric across all channels, and so a
     * future channel-specific concern (e.g. a websocket push on
     * creation) has an obvious place to live.
     * ================================================================
     */
    public void dispatchInApp() {
        // Intentionally empty -- see class-level comment.
    }
}
