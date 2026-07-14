package com.productadda.service.payment;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.entity.Order;
import com.productadda.entity.Payment;
import com.productadda.entity.PaymentAuditLog;
import com.productadda.entity.PaymentStatus;

import com.productadda.exception.ApiException;

import com.productadda.repository.OrderRepository;
import com.productadda.repository.PaymentAuditLogRepository;
import com.productadda.repository.PaymentRepository;
import com.productadda.repository.PaymentStatusRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * NEW SERVICE : PaymentServiceRazorpayWebhookProcess
 * API 2/15 - POST /api/payments/webhooks/razorpay
 * Granular, single-responsibility: Razorpay webhook processing only.
 * No Auth Required at the controller layer - HMAC signature
 * verification below is the trust boundary for this entire service.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceRazorpayWebhookProcess {

    private final PaymentRepository paymentRepository;
    private final PaymentAuditLogRepository paymentAuditLogRepository;
    private final OrderRepository orderRepository;
    private final PaymentStatusRepository paymentStatusRepository;

    private final RazorpayGatewayFetchOrderStatusService razorpayGatewayFetchOrderStatusService;

    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * PROCESS RAZORPAY WEBHOOK
     * ================================================================
     */
    @Transactional
    public void processWebhook(String rawRequestBody, String signatureHeader) {

        Map<String, Object> metaData = new HashMap<>();

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (signatureHeader == null || signatureHeader.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "X-Razorpay-Signature header is required");
        }
        if (rawRequestBody == null || rawRequestBody.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Webhook body is required");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        // Public endpoint by design. HMAC-SHA256 signature verification
        // below is the sole trust boundary for this service.
        String computedSignature = computeHmacSha256(rawRequestBody,
                razorpayGatewayFetchOrderStatusService.getWebhookSecret());
        if (!computedSignature.equals(signatureHeader)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Webhook signature verification failed");
        }

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        // TODO: extract webhookId, eventType, gatewayEntityId, gatewayStatus,
        // and failureReason from rawRequestBody via ObjectMapper into
        // RazorpayWebhookPayloadDto. JSON parsing intentionally stubbed
        // below pending a confirmed real Razorpay webhook sample payload.
        String gatewayEntityId = extractGatewayEntityId(rawRequestBody);
        Payment payment = paymentRepository.findByGatewayTransactionId(gatewayEntityId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Payment not found for gateway entity"));

        String webhookActionKey = "razorpay_webhook_" + extractWebhookId(rawRequestBody);
        if (paymentAuditLogRepository.existsByFkPaymentAndAction(payment, webhookActionKey)) {
            // Already processed this exact webhook delivery: acknowledge without
            // reprocessing.
            return;
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        String gatewayStatus = extractGatewayStatus(rawRequestBody);
        String oldStatus = payment.getFkStatus().getStatusName();
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

        boolean isSuccessEvent = "captured".equalsIgnoreCase(gatewayStatus)
                || "authorized".equalsIgnoreCase(gatewayStatus);
        boolean isFailureEvent = "failed".equalsIgnoreCase(gatewayStatus);

        if (!isSuccessEvent && !isFailureEvent) {
            // Unrecognized/unsupported event type: acknowledge and ignore, per webhook best
            // practice
            return;
        }

        PaymentStatus newStatus;
        if (isSuccessEvent) {
            // CONFIRMED value: PaymentServicePaymentVerify (legacy flow) checks
            // "SUCCESS".equals(status.getStatusName()) directly against real data.
            newStatus = paymentStatusRepository.findByStatusName("SUCCESS")
                    .orElseThrow(
                            () -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "SUCCESS status not configured"));
        } else {
            // TODO: "FAILED" is NOT confirmed against real payment_statuses data
            // (only "SUCCESS" and "PENDING" are used/implied by existing code).
            // Verify the exact status_name string for failures before deploying.
            newStatus = paymentStatusRepository.findByStatusName("FAILED")
                    .orElseThrow(
                            () -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED status not configured"));
        }

        payment.setFkStatus(newStatus);
        payment.setWebhookReceivedAtUtc(nowUtc);
        if (isSuccessEvent) {
            payment.setCapturedAtUtc(nowUtc);
        } else {
            payment.setFailedAtUtc(nowUtc);
            payment.setFailureReason(extractFailureReason(rawRequestBody));
        }

        Order order = payment.getFkOrder();
        if (isSuccessEvent) {
            // TODO: hook to inventory service to decrement stock / mark
            // order_items as 'processing' for every line item on this order.
            order.setPaymentConfirmedAtUtc(nowUtc);
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        payment = paymentRepository.save(payment);

        orderRepository.save(order);

        Map<String, Object> details = new HashMap<>();

        details.put("status", gatewayStatus);

        metaData.put("gatewayEvent", details);

        PaymentAuditLog auditLog = PaymentAuditLog.builder()
                .pkAuditLogId(uuidUtil.generateUuidV7())
                .fkPayment(payment)
                .action(webhookActionKey)
                .actorRole("SYSTEM")
                .oldStatus(oldStatus)
                .newStatus(newStatus.getStatusName())
                .metadata(metaData)
                .build();
                
        paymentAuditLogRepository.save(auditLog);

        // TODO: notification service hook - payment_confirmed event

        /*
         * ================================================================
         * 4. RESPONSE MAPPING
         * Note: Void return - controller returns a fixed acknowledgment
         * body to Razorpay regardless of outcome here.
         * ================================================================
         */
    }

    /*
     * HMAC-SHA256 computation for the signature trust-boundary check above.
     */
    private String computeHmacSha256(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Signature computation failed");
        }
    }

    // TODO: replace all three extraction stubs below with real Jackson
    // ObjectMapper parsing into RazorpayWebhookPayloadDto once a confirmed
    // sample Razorpay webhook payload is available.
    private String extractWebhookId(String rawRequestBody) {
        return "stub-webhook-id";
    }

    private String extractGatewayEntityId(String rawRequestBody) {
        return "stub-gateway-entity-id";
    }

    private String extractGatewayStatus(String rawRequestBody) {
        return "captured";
    }

    private String extractFailureReason(String rawRequestBody) {
        return null;
    }
}
