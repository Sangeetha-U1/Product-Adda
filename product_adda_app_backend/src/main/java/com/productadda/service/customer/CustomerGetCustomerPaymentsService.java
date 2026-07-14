package com.productadda.service.customer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.payment.PaginatedPaymentHistoryResponseDto;
import com.productadda.dto.payment.PaymentHistoryItemDto;

import com.productadda.entity.Order;
import com.productadda.entity.Payment;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.PaymentRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * CustomerPaymentRetrievalService
 * API 6/15 - GET /api/customers/payments
 * Mirrors CustomerOrderRetrievalService's structure and defense-in-
 * depth role re-check exactly, adapted for the payments domain.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class CustomerGetCustomerPaymentsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PaymentRepository paymentRepository;

    /*
     * ================================================================
     * GET CUSTOMER PAYMENTS
     * Description: Retrieves a paginated list of the authenticated
     * customer's own payments, with optional status, gateway, and
     * created-date range filters.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public PaginatedPaymentHistoryResponseDto getCustomerPayments(
            int page, int size, String status, String startDate, String endDate, String paymentGateway) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (page < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "page must be >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "size must be > 0 and <= 100");
        }

        String safeStatus = (status == null || status.trim().isEmpty())
                ? null
                : status.trim().toUpperCase();

        String safeGateway = (paymentGateway == null || paymentGateway.trim().isEmpty())
                ? null
                : paymentGateway.trim().toUpperCase();

        LocalDateTime startDateTime = null;
        if (startDate != null && !startDate.trim().isEmpty()) {
            try {
                startDateTime = LocalDate.parse(startDate.trim()).atStartOfDay();
            } catch (DateTimeParseException ex) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "startDate must be a valid ISO date (yyyy-MM-dd)");
            }
        }

        LocalDateTime endDateTime = null;
        if (endDate != null && !endDate.trim().isEmpty()) {
            try {
                endDateTime = LocalDate.parse(endDate.trim()).atTime(23, 59, 59);
            } catch (DateTimeParseException ex) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "endDate must be a valid ISO date (yyyy-MM-dd)");
            }
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
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Authenticated user no longer exists"));

        // Zero-Trust role re-check, mirroring CustomerOrderRetrievalService:
        // defense-in-depth even though the controller already carries
        // @PreAuthorize("hasAuthority('CUSTOMER')").
        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);

        if (userRoles.isEmpty()) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Customer role required to access this endpoint");
        }

        List<String> assignedRoleNames = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .collect(Collectors.toList());

        boolean hasCustomerRole = assignedRoleNames.stream()
                .anyMatch(roleName -> "CUSTOMER".equals(roleName));

        if (!hasCustomerRole) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Customer role required to access this endpoint");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAtUtc"));

        Page<Payment> paymentPage = paymentRepository.findCustomerPaymentsWithFilters(
                currentUser, safeStatus, safeGateway, startDateTime, endDateTime, pageRequest);

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * Reason: Read-only paginated lookup, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        List<PaymentHistoryItemDto> items = paymentPage.getContent().stream()
                .map(payment -> {

                    Order linkedOrder = payment.getFkOrder();

                    return PaymentHistoryItemDto.builder()
                            .paymentId(payment.getPkPaymentId().toString())
                            .orderId(linkedOrder != null ? linkedOrder.getPkOrderId().toString() : null)
                            .orderNumber(linkedOrder != null ? linkedOrder.getOrderNumber() : null)
                            .status(payment.getFkStatus() != null
                                    ? payment.getFkStatus().getStatusName()
                                    : "UNKNOWN")
                            .amountInPaise(payment.getAmountInPaise())
                            .paymentMethodDisplay(maskPaymentMethodForDisplay(payment.getPaymentMethod()))
                            .paymentGateway(payment.getFkGateway() != null
                                    ? payment.getFkGateway().getGatewayName()
                                    : "UNKNOWN")
                            .createdAtUtc(payment.getCreatedAtUtc())
                            .capturedAtUtc(payment.getCapturedAtUtc())
                            // TODO: refunds table does not exist;
                            // hardcoded to 0 until RefundRepository is available.
                            .refundedAmountInPaise(0L)
                            .build();
                })
                .collect(Collectors.toList());

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return PaginatedPaymentHistoryResponseDto.builder()
                .content(items)
                .totalElements(paymentPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    /*
     * TODO: Replace with a real "Card ending in XXXX" lookup once
     * PaymentMethod.java is available. Generic label only for now.
     * Duplicated from PaymentServiceGetPaymentStatus intentionally - these
     * are two independent granular services, not sharing a base class,
     * per the project's single-responsibility service convention.
     */
    private String maskPaymentMethodForDisplay(String paymentMethod) {
        if (paymentMethod == null) {
            return "Unknown";
        }
        return switch (paymentMethod.toLowerCase()) {
            case "card" -> "Card payment";
            case "upi" -> "UPI payment";
            case "netbanking" -> "Net banking payment";
            case "wallet" -> "Wallet payment";
            case "cod" -> "Cash on delivery";
            default -> paymentMethod;
        };
    }
}
