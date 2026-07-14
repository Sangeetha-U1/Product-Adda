package com.productadda.service.payment;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.payment.PaymentDetailResponseDto;

import com.productadda.entity.Payment;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.PaymentRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentsGetByPaymentService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PaymentRepository paymentRepository;

    /*
     * ================================================================
     * GET PAYMENT DETAILS BY PAYMENT ID
     * Description: Rich detail view for a single payment. Ownership-only
     * access -- the payment must belong to the authenticated user.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public PaymentDetailResponseDto getPaymentDetails(UUID paymentId) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (paymentId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "paymentId is required");
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
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);
        if (userRoles.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User role not found");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found"));

        if (payment.getFkUser() == null
                || !payment.getFkUser().getPkUserId().equals(currentUser.getPkUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Access denied: this payment does not belong to you");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Reason: Read-only lookup, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        String paymentMethodDisplay = maskPaymentMethodForDisplay(payment.getPaymentMethod());

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return PaymentDetailResponseDto.builder()
                .paymentId(payment.getPkPaymentId())
                .orderId(payment.getFkOrder() != null ? payment.getFkOrder().getPkOrderId() : null)
                .orderNumber(payment.getFkOrder() != null ? payment.getFkOrder().getOrderNumber() : null)
                .statusName(payment.getFkStatus() != null ? payment.getFkStatus().getStatusName() : "UNKNOWN")
                .paymentMethodDisplay(paymentMethodDisplay)
                .gatewayName(payment.getFkGateway() != null ? payment.getFkGateway().getGatewayName() : null)
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .gatewayOrderId(payment.getGatewayOrderId())
                .gatewayPaymentLinkId(payment.getGatewayPaymentLinkId())
                .amountInPaise(payment.getAmountInPaise())
                .currency(payment.getCurrency())
                .createdAtUtc(payment.getCreatedAtUtc())
                .paidAtUtc(payment.getPaidAtUtc())
                .capturedAtUtc(payment.getCapturedAtUtc())
                .failedAtUtc(payment.getFailedAtUtc())
                .failureReason(payment.getFailureReason())
                .build();
    }

    /*
     * Mirrors PaymentServiceGetPaymentStatus's masking helper so both
     * endpoints display payment methods identically.
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