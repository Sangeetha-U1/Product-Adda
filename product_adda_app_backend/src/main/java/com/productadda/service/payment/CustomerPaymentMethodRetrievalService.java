package com.productadda.service.payment;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.payment.PaymentMethodListDto;
import com.productadda.entity.PaymentMethod;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.PaymentMethodRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * CustomerPaymentMethodRetrievalService
 * API 11/14 - GET /api/customers/payment-methods
 * Mirrors CustomerPaymentRetrievalService's structure, adapted for
 * the (unpaginated, per the plan) saved-methods list.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class CustomerPaymentMethodRetrievalService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;

    /*
     * ================================================================
     * GET SAVED PAYMENT METHODS
     * ================================================================
     */
    @Transactional(readOnly = true)
    public List<PaymentMethodListDto> getSavedPaymentMethods(Boolean filterActive) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        // Note: filterActive is an optional Boolean - no format validation needed.

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
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        // Default to active-only when no explicit filter is supplied, per the plan.
        boolean effectiveFilterActive = filterActive != null ? filterActive : true;

        List<PaymentMethod> paymentMethods = paymentMethodRepository
                .findByFkUserAndIsActiveOrderByIsPrimaryDescCreatedAtUtcDesc(currentUser, effectiveFilterActive);

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * Reason: Read-only lookup, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        List<PaymentMethodListDto> items = paymentMethods.stream()
                .map(paymentMethod -> PaymentMethodListDto.builder()
                        .id(paymentMethod.getPkPaymentMethodId().toString())
                        .methodType(paymentMethod.getMethodType())
                        .displayName(buildDisplayName(paymentMethod))
                        .isPrimary(paymentMethod.getIsPrimary())
                        .isActive(paymentMethod.getIsActive())
                        .createdAtUtc(paymentMethod.getCreatedAtUtc())
                        .build())
                .collect(Collectors.toList());

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return items;
    }

    /*
     * PCI-safe display label. Duplicated from PaymentMethodServiceSave
     * intentionally - two independent granular services, not sharing a
     * base class, per the project's single-responsibility convention.
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
