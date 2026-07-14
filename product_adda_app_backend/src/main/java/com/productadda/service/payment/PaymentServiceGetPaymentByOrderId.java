package com.productadda.service.payment;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.payment.PaymentCreateResponseDto;

import com.productadda.entity.Order;
import com.productadda.entity.Payment;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.OrderRepository;
import com.productadda.repository.PaymentRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceGetPaymentByOrderId {

        private final UserRepository userRepository;
        private final OrderRepository orderRepository;
        private final PaymentRepository paymentRepository;

        /*
         * ================================================================
         * RETRIEVE PAYMENT DETAILS
         * Description: Retrieves existing payment details for the authenticated
         * customer's order. No payment mutation is performed.
         * ================================================================
         */
        @Transactional(readOnly = true)
        public PaymentCreateResponseDto getPaymentByOrderId(UUID orderId) {

                try {

                        /*
                         * ================================================================
                         * 1. VALIDATION SECTION
                         * ================================================================
                         */
                        // ==========================================
                        // 1.1 REQUEST VALIDATION
                        // ==========================================
                        if (orderId == null) {
                                throw new ApiException(
                                                HttpStatus.BAD_REQUEST,
                                                "Order ID must not be null");
                        }

                        // ==========================================
                        // 1.2 CONTEXT AUTHENTICATION
                        // ==========================================
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                        if (authentication == null || !authentication.isAuthenticated()) {
                                throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
                        }

                        String currentUsername = authentication.getName();

                        // ==========================================
                        // 1.3 DATABASE LOOKUP VALIDATION
                        // ==========================================
                        User currentUser = userRepository.findByEmail(currentUsername)
                                        .orElseThrow(() -> new ApiException(
                                                        HttpStatus.NOT_FOUND,
                                                        "Authenticated user no longer exists"));

                        Order order = orderRepository.findById(orderId)
                                        .orElseThrow(() -> new ApiException(
                                                        HttpStatus.NOT_FOUND,
                                                        "Order not found"));

                        if (order.getFkUser() == null
                                        || !order.getFkUser().getPkUserId().equals(currentUser.getPkUserId())) {

                                throw new ApiException(
                                                HttpStatus.FORBIDDEN,
                                                "Access denied: This order does not belong to authenticated user");
                        }

                        Payment payment = paymentRepository.findByFkOrder(order)
                                        .orElseThrow(() -> new ApiException(
                                                        HttpStatus.NOT_FOUND,
                                                        "Payment details not found for this order"));

                        /*
                         * ================================================================
                         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                         * ================================================================
                         */

                        /*
                         * ================================================================
                         * 3. DB SAVING SECTION
                         * Description: Read-only operation. No database mutation performed.
                         * ================================================================
                         */

                        /*
                         * ================================================================
                         * 4. POST-RETRIEVAL DATA SANITIZATION & MASKING
                         * ================================================================
                         */

                        /*
                         * ================================================================
                         * 5. RESPONSE MAPPING
                         * ================================================================
                         */

                        return PaymentCreateResponseDto.builder()
                                        .rawLinkDetails(payment.getMetadata())
                                        .build();

                } catch (ApiException ex) {
                        throw ex;

                } catch (IllegalArgumentException ex) {
                        throw new ApiException(
                                        HttpStatus.BAD_REQUEST,
                                        "Provided Order ID format is invalid");

                } catch (Exception ex) {
                        throw new ApiException(
                                        HttpStatus.BAD_GATEWAY,
                                        "Failed to retrieve payment details");
                }
        }
}