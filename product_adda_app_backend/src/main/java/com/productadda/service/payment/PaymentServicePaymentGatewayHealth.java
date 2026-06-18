package com.productadda.service.payment;

import jakarta.annotation.PostConstruct;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.RazorpayClient;
import com.productadda.dto.payment.PaymentGatewayHealthResponseDto;

@Service
public class PaymentServicePaymentGatewayHealth {

        private static final Logger log = LoggerFactory.getLogger(PaymentServicePaymentGatewayHealth.class);

        @Value("${razorpay.key-id}")
        private String apiKey;

        @Value("${razorpay.key-secret}")
        private String apiSecret;

        @Value("${payment.razorpay.mode:TEST}")
        private String gatewayMode;

        private RazorpayClient razorpayClient;

        /*
         * ================================================================
         * LIFECYCLE INITIALIZATION
         * Description: Initializes the SDK client once on container startup
         * instead of thrashing memory allocations per API call.
         * ================================================================
         */
        @PostConstruct
        public void init() {
                try {
                        this.razorpayClient = new RazorpayClient(apiKey, apiSecret);
                } catch (Exception e) {
                        log.error("Fatal: Failed to initialize RazorpayClient bean configuration. Check credentials.",
                                        e);
                }
        }

        /*
         * ================================================================
         * FETCH GATEWAY HEALTH STATUS
         * Description: Executes low-overhead fetch to verify system connectivity paths.
         * ================================================================
         */
        public PaymentGatewayHealthResponseDto getGatewayHealth() {
                String status;

                if (this.razorpayClient == null) {
                        return PaymentGatewayHealthResponseDto.builder()
                                        .gateway("RAZORPAY")
                                        .status("DOWN")
                                        .mode(gatewayMode)
                                        .build();
                }

                try {
                        JSONObject query = new JSONObject();
                        query.put("count", 1);

                        this.razorpayClient.orders.fetchAll(query);

                        status = "CONNECTED";
                } catch (Exception e) {
                        log.warn("Razorpay external health connectivity validation check failed: {}", e.getMessage());
                        status = "DOWN";
                }

                return PaymentGatewayHealthResponseDto.builder()
                                .gateway("RAZORPAY")
                                .status(status)
                                .mode(gatewayMode)
                                .build();
        }
}