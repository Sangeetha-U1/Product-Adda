package com.productadda.service.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import com.productadda.dto.payment.PaymentCreateRequestDto;
import com.productadda.dto.payment.PaymentCreateResponseDto;

import com.productadda.entity.Order;
import com.productadda.entity.Payment;
import com.productadda.entity.PaymentGateway;
import com.productadda.entity.PaymentStatus;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.OrderRepository;
import com.productadda.repository.PaymentGatewayRepository;
import com.productadda.repository.PaymentRepository;
import com.productadda.repository.PaymentStatusRepository;
import com.productadda.repository.UserRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServicePaymentCreate {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final PaymentGatewayRepository paymentGatewayRepository;
    private final UserRepository userRepository;
    private final RazorpayClient razorpayClient;
    private final UuidUtil uuidUtil;
    private final TransactionTemplate transactionTemplate;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public PaymentCreateResponseDto paymentCreate(PaymentCreateRequestDto requestDto) {
        try {
            /*
             * ================================================================
             * 1. VALIDATION SECTION
             * ================================================================
             */

            // ==========================================
            // 1.1 REQUEST VALIDATION
            // ==========================================
            if (requestDto == null || requestDto.getOrderId() == null || requestDto.getOrderId().trim().isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Order ID identifier must not be null or empty");
            }

            UUID orderId;
            try {
                orderId = UUID.fromString(requestDto.getOrderId());
            } catch (IllegalArgumentException e) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Provided Order ID layout string is invalid");
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
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

            if (order.getFkUser() == null || !order.getFkUser().getPkUserId().equals(currentUser.getPkUserId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: This order does not belong to user "
                        + email + " with user id " + currentUser.getPkUserId());
            }

            PaymentStatus pendingStatus = paymentStatusRepository.findByStatusName("PENDING")
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Payment status PENDING not found"));

            PaymentGateway razorpayGateway = paymentGatewayRepository.findByGatewayName("RAZORPAY")
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Payment gateway RAZORPAY configuration record missing from database"));

            /*
             * ================================================================
             * 2. BUSINESS RULES & PROCESSING / WORKFLOW
             * Description: Generates remote order link tokens externally on the Razorpay
             * network.
             * ================================================================
             */
            long amountInPaise = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

            JSONObject linkOptions = new JSONObject();
            linkOptions.put("amount", amountInPaise);
            // TODO: Add currency lookup table and add fk col in order and add here
            linkOptions.put("currency", "INR");
            linkOptions.put("description", "Verification Testing for Order " + orderId);
            linkOptions.put("reference_id", order.getPkOrderId().toString());
            linkOptions.put("callback_url", frontendBaseUrl);
            linkOptions.put("callback_method", "get");

            // External gateway communication safely isolated outside local DB locks
            com.razorpay.PaymentLink paymentLink = razorpayClient.paymentLink.create(linkOptions);
            String razorpayPaymentLinkId = paymentLink.get("id");

            /*
             * ================================================================
             * 3. DB SAVING SECTION
             * Description: Mounts the generated link state in the system using programmatic
             * isolation hooks.
             * ================================================================
             */
            LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

            transactionTemplate.executeWithoutResult(status -> {
                Payment payment = Payment.builder()
                        .pkPaymentId(uuidUtil.generateUuidV7())
                        .fkOrder(order)
                        .fkGateway(razorpayGateway)
                        // TODO: Create lookup table for payment methods
                        .paymentMethod("RAZORPAY")
                        .fkStatus(pendingStatus)
                        .fkPaymentSource(null)
                        .idempotencyKey(order.getIdempotencyKey())
                        .amountInPaise(amountInPaise)
                        .gatewayTransactionId(null)
                        .gatewayOrderId(null)
                        .gatewayPaymentLinkId(razorpayPaymentLinkId)
                        .gatewaySignature(null)
                        .paidAtUtc(null)
                        .metadata(null)
                        .capturedAtUtc(null)
                        .failedAtUtc(null)
                        .webhookReceivedAtUtc(null)
                        .failureReason(null)
                        .isActive(true)
                        .createdAtUtc(nowUtc)
                        .updatedAtUtc(nowUtc)
                        .build();

                paymentRepository.save(payment);
            });

            /*
             * ================================================================
             * 4. RESPONSE MAPPING
             * ================================================================
             */
            return PaymentCreateResponseDto.builder()
                    .rawLinkDetails(paymentLink.toJson().toMap())
                    .build();

        } catch (ApiException ex) {
            throw ex;
        } catch (RazorpayException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Razorpay Integration Failure: " + ex.getMessage());
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Failed to initialize payment process");
        }
    }
}