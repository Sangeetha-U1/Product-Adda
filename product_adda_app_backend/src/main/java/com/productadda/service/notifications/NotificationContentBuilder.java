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
     * Description: Hardcoded fallback content, used by
     * TemplateContentResolverService whenever no notification_templates
     * row matches (type, channel, role) -- whether because none was
     * ever seeded for that combination, or a future day's delete
     * feature removed it. context keys are snake_case, matching the template variable convention (customer_name, order_id, etc.),
     * so the same context map works whether it ends up rendered here or
     * through TemplateRenderingEngine.
     * ================================================================
     */
    public NotificationContentDto buildContent(String notificationTypeName, Map<String, Object> context) {

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
         * ================================================================
         */
        String subject;
        String body;

        switch (notificationTypeName) {
            case "ORDER_PLACED":
                subject = "Your order " + str(context, "order_id") + " has been placed";
                body = "Hi " + str(context, "customer_name") + ", we've received your order "
                        + str(context, "order_id") + " totalling Rs. " + str(context, "total_amount")
                        + ". We'll notify you as it progresses.";
                break;
            case "ORDER_CONFIRMED":
                subject = "Payment confirmed for order " + str(context, "order_id");
                body = "Hi " + str(context, "customer_name") + ", your payment for order "
                        + str(context, "order_id") + " has been confirmed. Your order is now being prepared.";
                break;
            case "PAYMENT_SUCCESS":
                subject = "Payment receipt for order " + str(context, "order_id");
                body = "Hi " + str(context, "customer_name") + ", we've received your payment of Rs. "
                        + str(context, "amount_in_paise") + " (transaction " + str(context, "transaction_id")
                        + ") for order " + str(context, "order_id") + ".";
                break;
            case "ORDER_PROCESSING":
                subject = "Order " + str(context, "order_id") + " is being processed";
                body = "Hi " + str(context, "customer_name") + ", the vendor(s) have started processing your order "
                        + str(context, "order_id") + ".";
                break;
            case "ORDER_SHIPPED":
                subject = "Order " + str(context, "order_id") + " has shipped";
                body = "Hi " + str(context, "customer_name") + ", all items in your order "
                        + str(context, "order_id") + " have shipped and are awaiting pickup.";
                break;
            case "ORDER_PICKED_UP":
                subject = "Order " + str(context, "order_id") + " picked up for delivery";
                body = "Hi " + str(context, "customer_name") + ", your order " + str(context, "order_id")
                        + " has been picked up by delivery partner " + str(context, "partner_name") + ".";
                break;
            case "ORDER_OUT_FOR_DELIVERY":
                subject = "Order " + str(context, "order_id") + " is out for delivery";
                body = "Hi " + str(context, "customer_name") + ", your order " + str(context, "order_id")
                        + " is out for delivery and should arrive soon.";
                break;
            case "ORDER_DELIVERED":
                subject = "Order " + str(context, "order_id") + " delivered";
                body = "Hi " + str(context, "customer_name") + ", your order " + str(context, "order_id")
                        + " has been delivered. We hope you enjoy your purchase!";
                break;
            case "ORDER_CANCELLED":
                subject = "Order " + str(context, "order_id") + " cancelled";
                body = "Hi " + str(context, "customer_name") + ", your order " + str(context, "order_id")
                        + " has been cancelled. Reason: " + str(context, "cancellation_reason") + ".";
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

    /*
     * ================================================================
     * STR (private helper)
     * Description: Null-safe context value stringification.
     * ================================================================
     */
    private String str(Map<String, Object> context, String key) {
        Object value = context.get(key);
        return value == null ? "" : String.valueOf(value);
    }
}
