package com.productadda.service.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.http.HttpStatus;
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
import com.productadda.entity.OrderStatus;
import com.productadda.exception.ApiException;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.PaymentRepository;
import com.productadda.repository.PaymentStatusRepository;
import com.productadda.repository.OrderStatusRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServicePaymentVerify {

        private final OrderRepository orderRepository;
        private final OrderStatusRepository orderStatusRepository;
        private final PaymentRepository paymentRepository;
        private final PaymentStatusRepository paymentStatusRepository;
        private final RazorpayConfig razorpayConfig;

        @Transactional
        public PaymentVerifyResponseDto paymentVerify(PaymentVerifyRequestDto requestDto) {
                try {

                        /*
                         * ================================================================
                         * 1. VALIDATION SECTION
                         * Description: Centralized block handling all input data integrity checks and
                         * database lookups.
                         * ================================================================
                         */

                        // ==========================================
                        // 1.1 DATABASE LOOKUP VALIDATION
                        // Description: Verifies existence of dependent target records within the
                        // database before running process logic.
                        // ==========================================
                        Payment payment = paymentRepository
                                        .findByRazorpayPaymentLinkId(requestDto.getRazorpayPaymentLinkId())
                                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                        "Payment record not found in DB"));

                        PaymentStatus status = payment.getFkStatus();

                        // ==========================================
                        // 1.2 REQUEST VALIDATION
                        // Description: Parses and ensures that incoming payload data parameters meet
                        // fundamental requirements.
                        // ==========================================
                        if (status != null && "SUCCESS".equals(status.getStatusName())) {
                                throw new ApiException(
                                                HttpStatus.BAD_REQUEST,
                                                "Payment already verified");
                        }

                        if (!Objects.equals(
                                        payment.getRazorpayPaymentLinkId(),
                                        requestDto.getRazorpayPaymentLinkId())) {

                                throw new ApiException(
                                                HttpStatus.BAD_REQUEST,
                                                "Payment Link ID mismatch");
                        }
                        /*
                         * ================================================================
                         * 2. BUSINESS SECTION
                         * Description: Initiates external integration with the Razorpay Payment
                         * Gateway,
                         * calculates pricing units, and configures external link instances.
                         * ================================================================
                         */
                        String paymentId = requestDto.getRazorpayPaymentId();
                        String paymentLinkId = payment.getRazorpayPaymentLinkId();
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

                        payment.setFkStatus(successStatus);
                        payment.setRazorpayPaymentId(paymentId);
                        payment.setRazorpayOrderId(razorpayFetchedOrderId);
                        payment.setRazorpaySignature(requestDto.getRazorpaySignature());
                        payment.setPaidAtUtc(LocalDateTime.now());

                        /*
                         * ================================================================
                         * 3. DB SAVING SECTION
                         * Description: Executes persistence validations and stores the final local
                         * payment entity to the database.
                         * ================================================================
                         */
                        paymentRepository.save(payment);

                        Order order = payment.getFkOrder();

                        if (order == null) {
                                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Order link broken");
                        }

                        order.setFkStatus(processingStatus);
                        orderRepository.save(order);

                        /*
                         * ================================================================
                         * 4. RESPONSE SECTION
                         * Description: Extracts generation properties into structural payloads for API
                         * presentation returns.
                         * ================================================================
                         */
                        return PaymentVerifyResponseDto.builder()
                                        .verified(true)
                                        .paymentStatus(payment.getFkStatus().getStatusName())
                                        .orderStatus(order.getFkStatus().getStatusName())
                                        .razorpayOrderId(payment.getRazorpayOrderId())
                                        .razorpayPaymentId(payment.getRazorpayPaymentId())
                                        .message("Payment verified and matched successfully!")
                                        .orderId(order.getPkOrderId().toString())
                                        .build();

                } catch (ApiException ex) {
                        throw ex;
                } catch (RazorpayException ex) {

                        // ex.printStackTrace();

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