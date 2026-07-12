package com.productadda.service.payment;

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
        private final RazorpayClient razorpayClient;
        private final RazorpayConfig razorpayConfig;

        @Transactional
        public PaymentVerifyResponseDto paymentVerify(PaymentVerifyRequestDto requestDto) {
                try {
                        /*
                         * ================================================================
                         * 1. VALIDATION SECTION
                         * ================================================================
                         */

                        // ==========================================
                        // 1.1 REQUEST VALIDATION
                        // ==========================================
                        if (requestDto == null) {
                                throw new ApiException(HttpStatus.BAD_REQUEST,
                                                "Verification request body must not be null");
                        }
                        if (requestDto.getRazorpayPaymentLinkId() == null
                                        || requestDto.getRazorpayPaymentLinkId().trim().isEmpty()) {
                                throw new ApiException(HttpStatus.BAD_REQUEST,
                                                "Razorpay Payment Link ID missing from payload");
                        }
                        if (requestDto.getRazorpayPaymentId() == null
                                        || requestDto.getRazorpayPaymentId().trim().isEmpty()) {
                                throw new ApiException(HttpStatus.BAD_REQUEST,
                                                "Razorpay Payment ID missing from payload");
                        }
                        if (requestDto.getRazorpaySignature() == null
                                        || requestDto.getRazorpaySignature().trim().isEmpty()) {
                                throw new ApiException(HttpStatus.BAD_REQUEST,
                                                "Razorpay Signature string missing from payload");
                        }

                        // ==========================================
                        // 1.2 CONTEXT AUTHENTICATION
                        // ==========================================
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        if (authentication == null || !authentication.isAuthenticated()) {
                                throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
                        }

                        String email;
                        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                                email = userDetails.getUsername();
                        } else {
                                email = authentication.getName();
                        }

                        // ==========================================
                        // 1.3 DATABASE LOOKUP VALIDATION
                        // ==========================================
                        User currentUser = userRepository.findByEmail(email)
                                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                        "Authenticated user no longer exists"));

                        Payment payment = paymentRepository
                                        .findByGatewayPaymentLinkId(requestDto.getRazorpayPaymentLinkId())
                                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                        "Payment record not found in DB"));

                        Order order = payment.getFkOrder();
                        if (order == null) {
                                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Payment association order link broken");
                        }

                        if (order.getFkUser() == null
                                        || !order.getFkUser().getPkUserId().equals(currentUser.getPkUserId())) {
                                throw new ApiException(HttpStatus.FORBIDDEN,
                                                "Access denied: You do not own the order linked to this payment link");
                        }

                        PaymentStatus status = payment.getFkStatus();
                        if (status != null && "SUCCESS".equals(status.getStatusName())) {
                                throw new ApiException(HttpStatus.BAD_REQUEST, "Payment already verified");
                        }

                        if (!Objects.equals(payment.getGatewayPaymentLinkId(), requestDto.getRazorpayPaymentLinkId())) {
                                throw new ApiException(HttpStatus.BAD_REQUEST, "Payment Link ID mismatch");
                        }

                        /*
                         * ================================================================
                         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
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
                                                "Invalid Razorpay signature signature verification mismatch");
                        }

                        // API call out using single shared Client dependency bean mapping
                        com.razorpay.Payment razorpayPayment = razorpayClient.payments.fetch(paymentId);
                        String razorpayStatus = razorpayPayment.get("status");

                        if (!"captured".equalsIgnoreCase(razorpayStatus)) {
                                throw new ApiException(HttpStatus.BAD_REQUEST,
                                                "Payment not captured by Razorpay infrastructure");
                        }

                        String razorpayFetchedOrderId = razorpayPayment.get("order_id");
                        long razorpayAmountInPaise = ((Number) razorpayPayment.get("amount")).longValue();
                        long dbAmountInPaise = payment.getAmountInPaise();

                        if (dbAmountInPaise != razorpayAmountInPaise) {
                                throw new ApiException(HttpStatus.BAD_REQUEST,
                                                "Razorpay order amount mismatch validation failure");
                        }

                        PaymentStatus successStatus = paymentStatusRepository.findByStatusName("SUCCESS")
                                        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                        "Status SUCCESS not found"));

                        OrderStatus paidStatus = orderStatusRepository.findByStatusName("PAID")
                                        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                        "Status PAID not found"));

                        payment.setFkStatus(successStatus);
                        payment.setGatewayTransactionId(paymentId);
                        payment.setGatewayOrderId(razorpayFetchedOrderId);
                        payment.setGatewaySignature(requestDto.getRazorpaySignature());
                        payment.setPaidAtUtc(LocalDateTime.now(ZoneOffset.UTC));
                        
                        order.setFkStatus(paidStatus);

                        /*
                         * ================================================================
                         * 3. DB SAVING SECTION
                         * ================================================================
                         */
                        paymentRepository.save(payment);
                        orderRepository.save(order);

                        /*
                         * ================================================================
                         * 4. RESPONSE MAPPING
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
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Razorpay Integration Failure: " + ex.getMessage());
                } catch (Exception ex) {
                        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                        "Verification processing structural validation loop failed");
                }
        }
}