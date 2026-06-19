package com.productadda.service.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import com.productadda.config.RazorpayConfig;
import com.productadda.dto.payment.PaymentVerifyRequestDto;
import com.productadda.dto.payment.PaymentVerifyResponseDto;
import com.productadda.entity.Order;
import com.productadda.entity.Payment;
import com.productadda.entity.PaymentStatus;
import com.productadda.entity.User;
import com.productadda.entity.OrderStatus;
import com.productadda.exception.ApiException;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.PaymentRepository;
import com.productadda.repository.PaymentStatusRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.OrderStatusRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServicePaymentVerify {

        private final OrderRepository orderRepository;
        private final OrderStatusRepository orderStatusRepository;
        private final PaymentRepository paymentRepository;
        private final PaymentStatusRepository paymentStatusRepository;
        private final UserRepository userRepository;
        private final RazorpayConfig razorpayConfig;

        @Transactional
        public PaymentVerifyResponseDto paymentVerify(PaymentVerifyRequestDto requestDto) {
                try {

                        /*
                         * ================================================================
                         * 1. ISOLATED SECURITY & VALIDATION SECTION
                         * Description: Independent database verification. Even if upstream layers are
                         * bypassed, this block guarantees session data integrity and strict ownership.
                         * ================================================================
                         */

                        // ==========================================
                        // 1.1 Read email from Security Context
                        // ==========================================
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                        if (authentication == null || !authentication.isAuthenticated()) {
                                throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
                        }

                        String email;

                        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                                email = userDetails.getUsername();
                        } else {
                                email = authentication.getPrincipal().toString();
                        }

                        // ==========================================
                        // 1.2 Isolated Database Check: Fetch fresher user record directly from DB
                        // ==========================================
                        User currentUser = userRepository.findByEmail(email)
                                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                        "Authenticated user no longer exists"));

                        // ==========================================
                        // 1.3 DATABASE LOOKUP VALIDATION (Payment & Order)
                        // ==========================================
                        Payment payment = paymentRepository
                                        .findByGatewayPaymentLinkId(requestDto.getRazorpayPaymentLinkId())
                                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                        "Payment record not found in DB"));

                        Order order = payment.getFkOrder();
                        if (order == null) {
                                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Payment association order link broken");
                        }

                        // ==========================================
                        // 1.4 Strict Ownership Validation: Check if the payment/order belongs to
                        // current user
                        // ==========================================
                        if (order.getFkUser() == null
                                        || !order.getFkUser().getPkUserId().equals(currentUser.getPkUserId())) {
                                throw new ApiException(HttpStatus.FORBIDDEN,
                                                "Access denied: You do not own the order linked to this payment link");
                        }

                        // ==========================================
                        // 1.5 PAYMENT STATUS STATE INTEGRITY CHECKS
                        // ==========================================
                        PaymentStatus status = payment.getFkStatus();
                        if (status != null && "SUCCESS".equals(status.getStatusName())) {
                                throw new ApiException(
                                                HttpStatus.BAD_REQUEST,
                                                "Payment already verified");
                        }

                        if (!Objects.equals(
                                        payment.getGatewayPaymentLinkId(),
                                        requestDto.getRazorpayPaymentLinkId())) {

                                throw new ApiException(
                                                HttpStatus.BAD_REQUEST,
                                                "Payment Link ID mismatch");
                        }

                        /*
                         * ================================================================
                         * 2. BUSINESS SECTION (Signature verification & Razorpay capture check)
                         * ================================================================
                         */
                        String paymentId = requestDto.getRazorpayPaymentId();
                        String paymentLinkId = payment.getGatewayPaymentLinkId();
                        String referenceId = requestDto.getRazorpayPaymentLinkReferenceId();
                        String linkStatus = requestDto.getRazorpayPaymentLinkStatus();

                        String payload = paymentLinkId + "|" + referenceId + "|" + linkStatus + "|" + paymentId;

                        boolean signatureValid = Utils.verifySignature(
                                        payload,
                                        requestDto.getRazorpaySignature(),
                                        razorpayConfig.getKeySecret());

                        if (!signatureValid) {
                                throw new ApiException(HttpStatus.BAD_REQUEST,
                                                "Invalid Razorpay signature");
                        }

                        RazorpayClient razorpayClient = new RazorpayClient(
                                        razorpayConfig.getKeyId(),
                                        razorpayConfig.getKeySecret());

                        // API call to Razorpay to get payment details
                        com.razorpay.Payment razorpayPayment = razorpayClient.payments.fetch(paymentId);

                        String razorpayStatus = razorpayPayment.get("status");

                        if (!"captured".equalsIgnoreCase(razorpayStatus)) {
                                throw new ApiException(
                                                HttpStatus.BAD_REQUEST,
                                                "Payment not captured by Razorpay");
                        }

                        // Extract the real order ID generated dynamically by Razorpay's link checkout
                        String razorpayFetchedOrderId = razorpayPayment.get("order_id");

                        long razorpayAmountInPaise = ((Number) razorpayPayment.get("amount")).longValue();

                        long dbAmountInPaise = payment.getAmountPaid()
                                        .multiply(BigDecimal.valueOf(100))
                                        .longValue();

                        if (dbAmountInPaise != razorpayAmountInPaise) {
                                throw new ApiException(
                                                HttpStatus.BAD_REQUEST,
                                                "Razorpay order amount mismatch");
                        }

                        PaymentStatus successStatus = paymentStatusRepository.findByStatusName("SUCCESS")
                                        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                        "Status SUCCESS not found"));

                        OrderStatus processingStatus = orderStatusRepository.findByStatusName("PROCESSING")
                                        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                        "Status PROCESSING not found"));

                        // Update Payment record fields using generic gateway-agnostic mapping
                        // properties
                        payment.setFkStatus(successStatus);
                        payment.setGatewayTransactionId(paymentId);
                        payment.setGatewayOrderId(razorpayFetchedOrderId);
                        payment.setGatewaySignature(requestDto.getRazorpaySignature());

                        payment.setPaidAtUtc(LocalDateTime.now(ZoneOffset.UTC));

                        /*
                         * ================================================================
                         * 3. DB SAVING SECTION
                         * ================================================================
                         */
                        paymentRepository.save(payment);

                        order.setFkStatus(processingStatus);

                        orderRepository.save(order);

                        /*
                         * ================================================================
                         * 4. RESPONSE SECTION
                         * ================================================================
                         */
                        return PaymentVerifyResponseDto.builder()
                                        .verified(true)
                                        .paymentStatus(payment.getFkStatus().getStatusName())
                                        .orderStatus(order.getFkStatus().getStatusName())
                                        .razorpayOrderId(payment.getGatewayOrderId())
                                        .razorpayPaymentId(payment.getGatewayTransactionId())
                                        .message("Payment verified and matched successfully!")
                                        .orderId(order.getPkOrderId().toString())
                                        .build();

                } catch (ApiException ex) {
                        throw ex;
                } catch (RazorpayException ex) {
                        String razorpayErrorMessage = ex.getMessage();
                        throw new ApiException(
                                        HttpStatus.BAD_REQUEST,
                                        "Razorpay Integration Failure: " + razorpayErrorMessage);
                } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Verification processing failed");
                }
        }
}