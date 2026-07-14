package com.productadda.service.payment;

import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.payment.PaymentMethodRequestDto;
import com.productadda.dto.payment.PaymentMethodResponseDto;

import com.productadda.entity.PaymentAuditLog;
import com.productadda.entity.PaymentMethod;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.PaymentAuditLogRepository;
import com.productadda.repository.PaymentMethodRepository;
import com.productadda.repository.UserRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * PaymentMethodServiceSave
 * API 10/14 - POST /api/payment-methods/save
 * PCI-compliant: only gatewayTokenId + masked card metadata are
 * ever persisted, never full card numbers or CVV.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class PaymentMethodServiceSave {

    // TODO: Replace string literals for method types with an enum
    // (CARD, UPI, NETBANKING, WALLET) after the API contract is finalized.

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentAuditLogRepository paymentAuditLogRepository;
    private final UserRepository userRepository;
    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * SAVE PAYMENT METHOD
     * ================================================================
     */
    @Transactional
    public PaymentMethodResponseDto savePaymentMethod(PaymentMethodRequestDto request) {

        Map<String, Object> metaData = new HashMap<>();

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        // Razorpay only, per project scope (see CHANGES_DAY4.md)
        if (!"razorpay".equalsIgnoreCase(request.getPaymentGateway())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "paymentGateway must be razorpay");
        }

        if (!isValidMethodType(request.getMethodType())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "methodType must be one of: card, upi, netbanking, wallet");
        }

        if (request.getGatewayTokenId() == null || request.getGatewayTokenId().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "gatewayTokenId is required");
        }

        if ("card".equalsIgnoreCase(request.getMethodType())) {
            if (request.getCardLastFour() == null || !request.getCardLastFour().matches("\\d{4}")) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "cardLastFour must be exactly 4 digits");
            }
            if (request.getCardExpiryMonth() == null || request.getCardExpiryMonth() < 1
                    || request.getCardExpiryMonth() > 12) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid card expiry month (must be 1-12)");
            }
            int currentYear = Year.now().getValue();
            if (request.getCardExpiryYear() == null || request.getCardExpiryYear() < currentYear) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Invalid card expiry year (must be current year or later)");
            }
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

        User authenticatedCustomer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        boolean alreadySaved = paymentMethodRepository
                .findByFkUserAndGatewayTokenId(authenticatedCustomer, request.getGatewayTokenId())
                .isPresent();
        if (alreadySaved) {
            throw new ApiException(HttpStatus.CONFLICT, "This payment method is already saved");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        boolean shouldBePrimary = Boolean.TRUE.equals(request.getIsPrimary());

        if (shouldBePrimary) {
            List<PaymentMethod> currentPrimaryMethods = paymentMethodRepository
                    .findByFkUserAndIsPrimaryTrue(authenticatedCustomer);
            for (PaymentMethod existingPrimary : currentPrimaryMethods) {
                existingPrimary.setIsPrimary(false);
                paymentMethodRepository.save(existingPrimary);
            }
        }

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .pkPaymentMethodId(uuidUtil.generateUuidV7())
                .fkUser(authenticatedCustomer)
                .paymentGateway(request.getPaymentGateway().toUpperCase())
                .gatewayTokenId(request.getGatewayTokenId())
                .methodType(request.getMethodType().toLowerCase())
                .cardLastFour(request.getCardLastFour())
                .cardBrand(request.getCardBrand())
                .cardExpiryMonth(request.getCardExpiryMonth())
                .cardExpiryYear(request.getCardExpiryYear())
                .isPrimary(shouldBePrimary)
                .isActive(true)
                .build();

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        paymentMethod = paymentMethodRepository.save(paymentMethod);

        Map<String, Object> details = new HashMap<>();

        details.put("methodType", paymentMethod.getMethodType());
        details.put("isPrimary", shouldBePrimary);

        metaData.put("paymentMethod", details);

        PaymentAuditLog auditLog = PaymentAuditLog.builder()
                .pkAuditLogId(uuidUtil.generateUuidV7())
                .fkActor(authenticatedCustomer)
                .actorRole("CUSTOMER")
                .action("payment_method_saved")
                .metadata(metaData)
                .build();
        paymentAuditLogRepository.save(auditLog);

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        String displayName = buildDisplayName(paymentMethod);
        String expiry = "card".equalsIgnoreCase(paymentMethod.getMethodType())
                ? String.format("%02d/%d", paymentMethod.getCardExpiryMonth(), paymentMethod.getCardExpiryYear())
                : null;

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return PaymentMethodResponseDto.builder()
                .id(paymentMethod.getPkPaymentMethodId().toString())
                .methodType(paymentMethod.getMethodType())
                .displayName(displayName)
                .brand(paymentMethod.getCardBrand())
                .expiry(expiry)
                .isPrimary(paymentMethod.getIsPrimary())
                .createdAtUtc(paymentMethod.getCreatedAtUtc())
                .build();
    }

    private boolean isValidMethodType(String methodType) {
        return "card".equalsIgnoreCase(methodType)
                || "upi".equalsIgnoreCase(methodType)
                || "netbanking".equalsIgnoreCase(methodType)
                || "wallet".equalsIgnoreCase(methodType);
    }

    /*
     * PCI-safe display label. Never includes gatewayTokenId or any raw
     * card data beyond the last four digits already stored.
     */
    private String buildDisplayName(PaymentMethod paymentMethod) {
        return switch (paymentMethod.getMethodType().toLowerCase()) {
            case "card" -> "Card ending in " + paymentMethod.getCardLastFour();
            case "upi" -> "UPI payment method";
            case "netbanking" -> "Net banking payment method";
            case "wallet" -> "Wallet payment method";
            default -> "Saved payment method";
        };
    }
}
