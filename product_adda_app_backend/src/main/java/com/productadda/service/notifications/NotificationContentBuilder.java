package com.productadda.service.notifications;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.productadda.dto.notifications.NotificationContentDto;

import com.productadda.exception.ApiException;

@Service
public class NotificationContentBuilder {

    /*
     * ================================================================
     * BUILD CONTENT
     * Description: Produces a rendered subject/body pair for a given
     * notification type using plain Java string formatting. This is a
     * Day 1 placeholder for the Day 3 notification_templates lookup --
     * the method signature (type name + context map in, subject/body
     * out) is intentionally stable so EventListenerService* callers
     * never need to change when Day 3 swaps this implementation for a
     * real template render.
     * ================================================================
     */
    public NotificationContentDto buildContent(String notificationTypeName, Map<String, String> context) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */
        if (notificationTypeName == null || notificationTypeName.trim().isEmpty()) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "notificationTypeName is required to build content");
        }
        if (context == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "context map is required to build content");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * TODO: Replace this switch with a notification_templates lookup
         * on Day 3 -- (event_type, channel, recipient_role) -> render.
         * ================================================================
         */
        String subject;
        String body;

        switch (notificationTypeName) {
            case "ORDER_PLACED":
                subject = "Your order " + context.get("orderNumber") + " has been placed";
                body = "Hi " + context.get("customerName") + ", we've received your order "
                        + context.get("orderNumber") + " totalling Rs. " + context.get("totalAmount")
                        + ". We'll notify you as it progresses.";
                break;
            case "ORDER_CONFIRMED":
                subject = "Payment confirmed for order " + context.get("orderNumber");
                body = "Hi " + context.get("customerName") + ", your payment for order "
                        + context.get("orderNumber") + " has been confirmed. Your order is now being prepared.";
                break;
            case "PAYMENT_SUCCESS":
                subject = "Payment receipt for order " + context.get("orderNumber");
                body = "Hi " + context.get("customerName") + ", we've received your payment of Rs. "
                        + context.get("amount") + " (transaction " + context.get("transactionId")
                        + ") for order " + context.get("orderNumber") + ".";
                break;
            case "ORDER_PROCESSING":
                subject = "Order " + context.get("orderNumber") + " is being processed";
                body = "Hi " + context.get("customerName") + ", the vendor(s) have started processing your order "
                        + context.get("orderNumber") + ".";
                break;
            case "ORDER_SHIPPED":
                subject = "Order " + context.get("orderNumber") + " has shipped";
                body = "Hi " + context.get("customerName") + ", all items in your order "
                        + context.get("orderNumber") + " have shipped and are awaiting pickup.";
                break;
            case "ORDER_PICKED_UP":
                subject = "Order " + context.get("orderNumber") + " picked up for delivery";
                body = "Hi " + context.get("customerName") + ", your order " + context.get("orderNumber")
                        + " has been picked up by delivery partner " + context.get("partnerName") + ".";
                break;
            case "ORDER_OUT_FOR_DELIVERY":
                subject = "Order " + context.get("orderNumber") + " is out for delivery";
                body = "Hi " + context.get("customerName") + ", your order " + context.get("orderNumber")
                        + " is out for delivery and should arrive soon.";
                break;
            case "ORDER_DELIVERED":
                subject = "Order " + context.get("orderNumber") + " delivered";
                body = "Hi " + context.get("customerName") + ", your order " + context.get("orderNumber")
                        + " has been delivered. We hope you enjoy your purchase!";
                break;
            case "ORDER_CANCELLED":
                subject = "Order " + context.get("orderNumber") + " cancelled";
                body = "Hi " + context.get("customerName") + ", your order " + context.get("orderNumber")
                        + " has been cancelled. Reason: " + context.get("cancellationReason") + ".";
                break;
            default:
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "No content mapping defined for notification type: " + notificationTypeName);
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Reason: N/A - pure in-memory content construction, no
         * persistence performed by this method.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: N/A - see section 3.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return NotificationContentDto.builder()
                .subject(subject)
                .body(body)
                .build();
    }
}
