package com.productadda.service.payment;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.razorpay.RazorpayClient;
import com.productadda.dto.payment.PaymentGatewayHealthResponseDto;

@Service
public class PaymentServicePaymentGatewayHealth {

        @Value("${razorpay.key-id}")
        private String apiKey;

        @Value("${razorpay.key-secret}")
        private String apiSecret;

        @Value("${payment.razorpay.mode:TEST}")
        private String gatewayMode;

        public PaymentGatewayHealthResponseDto getGatewayHealth() {
                String status;

                try {
                        RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecret);

                        JSONObject query = new JSONObject();

                        query.put("count", 1);

                        razorpay.orders.fetchAll(query);

                        status = "CONNECTED";
                } catch (Exception e) {
                        status = "DOWN";
                }

                return PaymentGatewayHealthResponseDto.builder()
                                .gateway("RAZORPAY")
                                .status(status)
                                .mode(gatewayMode)
                                .build();
        }
}