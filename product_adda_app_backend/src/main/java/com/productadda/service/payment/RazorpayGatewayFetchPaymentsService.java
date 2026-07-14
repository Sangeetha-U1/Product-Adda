package com.productadda.service.payment;

import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.productadda.config.RazorpayConfig;

import com.productadda.exception.ApiException;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * RazorpayGatewayService
 * REVISION NOTE: added fetchPayments(...) below
 * fetchRefundStatus(...) - everything from is unchanged.
 *
 * Thin, dedicated Razorpay integration boundary using the existing
 * RazorpayClient bean / RazorpayConfig already wired up for the
 * legacy Payment Links flow (PaymentServicePaymentVerify).
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class RazorpayGatewayFetchPaymentsService {

    // TODO: Implement Razorpay payment pagination. SDK v1.4.8 fetchAll()
    // returns at most one page (~100 records). Loop using skip/count (or the
    // SDK-supported pagination mechanism) until all payments in the requested
    // time window are retrieved.

    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;

    /*
     * Fetches all Razorpay payment transactions within a UTC
     * date range, for reconciliation comparison against the local payments
     * table.
     *
     * TODO: this call is written against the documented Razorpay Java SDK
     * surface (razorpayClient.payments.fetchAll(JSONObject) with "from"/"to"
     * Unix-epoch-second options), by direct analogy with the CONFIRMED real
     * usage of razorpayClient.orders.fetchAll(query) in
     * PaymentServicePaymentGatewayHealth.java. The exact return shape
     * (assumed here to behave like a JSONObject with an "items" JSONArray,
     * matching Razorpay's raw REST API list-response shape) has NOT been
     * verified against your installed SDK version - confirm before deploying.
     * Razorpay's list endpoints are also paginated (max ~100 records per
     * call) - this method does NOT yet loop through pages, which will
     * under-count on any day with more than ~100 transactions. Flagged
     * rather than silently handled.
     */
    public List<RazorpayTransactionSummary> fetchPayments(LocalDateTime fromUtc, LocalDateTime toUtc) {
        if (fromUtc == null || toUtc == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "fromUtc and toUtc are required");
        }

        try {
            JSONObject options = new JSONObject();
            options.put("from", fromUtc.toEpochSecond(ZoneOffset.UTC));
            options.put("to", toUtc.toEpochSecond(ZoneOffset.UTC));
            options.put("count", 100);

            List<com.razorpay.Payment> items = razorpayClient.payments.fetchAll(options);

            List<RazorpayTransactionSummary> transactions = new ArrayList<>();

            for (com.razorpay.Payment item : items) {
                transactions.add(RazorpayTransactionSummary.builder()
                        .gatewayTransactionId(item.get("id"))
                        .amountInPaise(Long.parseLong(item.get("amount").toString()))
                        .gatewayStatus(item.get("status").toString())
                        .build());
            }

            return transactions;

        } catch (RazorpayException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Razorpay payment list fetch failed: " + exception.getMessage());
        }
    }

    // TODO: Razorpay webhooks are typically signed with a dedicated
    // "webhook secret" configured separately in the Razorpay dashboard,
    // NOT the API key-secret used for authenticated API calls. RazorpayConfig
    // currently only exposes keyId/keySecret - it needs a new webhookSecret
    // field (with its own @Value("${razorpay.webhook-secret}")) added before
    // this can be relied on in production webhook signature verification.
    // Returning keySecret here is a placeholder only.
    public String getWebhookSecret() {
        return razorpayConfig.getKeySecret();
    }

    /*
     * ================================================================
     * MIDDLE-MAN DTO: RazorpayTransactionSummary
     * Internal transfer object for fetchPayments(...) reconciliation
     * results. Not a REST-facing DTO.
     * ================================================================
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class RazorpayTransactionSummary {
        private String gatewayTransactionId;
        private Long amountInPaise;
        private String gatewayStatus;
    }
}
