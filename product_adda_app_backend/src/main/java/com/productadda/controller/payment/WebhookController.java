package com.productadda.controller.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.productadda.dto.ApiSuccessResponseDto;

import com.productadda.service.payment.PaymentServiceRazorpayWebhookProcess;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * WebhookController
 * Razorpay only
 * No Auth Required - HMAC-SHA256 signature verification inside the
 * service is the trust boundary, not Spring Security. This path
 * MUST be added to PublicRoutes.CONFIGS (see CHANGES_DAY1.md for
 * the exact entry to add).
 * ================================================================
 */
@RestController
@RequestMapping("/api/payments/webhooks")
@RequiredArgsConstructor
public class WebhookController {

        private final PaymentServiceRazorpayWebhookProcess paymentServiceRazorpayWebhookProcess;

        @PostMapping("/razorpay")
        public ResponseEntity<ApiSuccessResponseDto<Void>> razorpayWebhook(
                        @RequestBody String rawRequestBody,
                        @RequestHeader("X-Razorpay-Signature") String signatureHeader) {

                paymentServiceRazorpayWebhookProcess.processWebhook(rawRequestBody, signatureHeader);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<Void>builder()
                                                                .success(true)
                                                                .message("Webhook received")
                                                                .data(null)
                                                                .build());
        }
}
