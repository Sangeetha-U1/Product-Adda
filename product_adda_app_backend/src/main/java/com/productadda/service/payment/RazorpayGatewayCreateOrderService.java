package com.productadda.service.payment;

import java.util.UUID;

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
 * fetchRefundStatus(...) - everything from Day 1-3 is unchanged.
 *
 * Thin, dedicated Razorpay integration boundary using the existing
 * RazorpayClient bean / RazorpayConfig already wired up for the
 * legacy Payment Links flow (PaymentServicePaymentVerify).
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class RazorpayGatewayCreateOrderService {

    // TODO: Introduce a payment gateway abstraction/interface to support
    // multiple gateways without changing reconciliation or payment services.

    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;

    /*
     * Creates a Razorpay Order for the given amount via the Orders API.
     * Returns the gateway order id and the frontend checkout key.
     */
    public RazorpayOrderResult createOrder(Long amountInPaise, UUID orderId) {
        if (amountInPaise == null || amountInPaise <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "amountInPaise must be greater than zero");
        }

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", orderId.toString());

            // com.razorpay.Order is fully qualified to avoid a naming clash
            // with our own com.productadda.entity.Order
            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String gatewayOrderId = razorpayOrder.get("id");

            return RazorpayOrderResult.builder()
                    .gatewayOrderId(gatewayOrderId)
                    .checkoutRedirectUrl("https://checkout.razorpay.com/v1/checkout.js?order_id=" + gatewayOrderId)
                    .checkoutKey(razorpayConfig.getKeyId())
                    .build();

        } catch (RazorpayException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Razorpay order creation failed: " + exception.getMessage());
        }
    }

    /*
     * ================================================================
     * MIDDLE-MAN DTO: RazorpayOrderResult
     * ================================================================
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class RazorpayOrderResult {
        private String gatewayOrderId;
        private String checkoutRedirectUrl;
        private String checkoutKey;
    }

}
