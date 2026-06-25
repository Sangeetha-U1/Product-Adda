package com.productadda.service.payment;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.RazorpayClient;
import com.productadda.dto.payment.PaymentGatewayHealthResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServicePaymentGatewayHealth {

        private static final Logger log = LoggerFactory.getLogger(PaymentServicePaymentGatewayHealth.class);

        private final RazorpayClient razorpayClient;

        @Value("${payment.razorpay.mode:TEST}")
        private String gatewayMode;

        /*
         * ================================================================
         * FETCH GATEWAY HEALTH STATUS
         * Description: Executes low-overhead fetch to verify system connectivity paths.
         * ================================================================
         */
        public PaymentGatewayHealthResponseDto getGatewayHealth() {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                // Note: Parameterless tracking endpoint methodology context.

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                // Note: Public or multi-role diagnostic request trace line. No runtime context
                // validation required.

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                // Note: Direct external infrastructure telemetry ping. No local storage
                // verification needed.

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                String status;

                try {
                        JSONObject query = new JSONObject();
                        query.put("count", 1);

                        // Ping Razorpay API using the injected singleton bean
                        this.razorpayClient.orders.fetchAll(query);
                        status = "CONNECTED";
                } catch (Exception e) {
                        log.warn("Razorpay external health connectivity validation check failed: {}", e.getMessage());
                        status = "DOWN";
                }

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * Note: Non-mutating telemetry monitoring execution pipeline.
                 * ================================================================
                 */

                /*
                 * ================================================================
                 * 4. RESPONSE MAPPING
                 * ================================================================
                 */
                return PaymentGatewayHealthResponseDto.builder()
                                .gateway("RAZORPAY")
                                .status(status)
                                .mode(gatewayMode)
                                .build();
        }
}