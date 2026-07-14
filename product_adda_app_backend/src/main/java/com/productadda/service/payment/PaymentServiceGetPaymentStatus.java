package com.productadda.service.payment;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.payment.PaymentStatusResponseDto;

import com.productadda.entity.Payment;
import com.productadda.entity.User;
import com.productadda.entity.Vendor;

import com.productadda.exception.ApiException;

import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.PaymentRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * PaymentServiceGetPaymentStatus
 * GET /api/payments/{paymentId}
 * Role-based access (customer/vendor/admin) with PCI masking.
 *
 * TODO: The vendor-ownership check below assumes
 * OrderItemRepository.findDistinctOrderIdsByVendorId(UUID) exists,
 * which is referenced (but not shown to me in full) via a comment
 * in the confirmed real OrderRepository.java. It also assumes a
 * VendorRepository.findByFkUser(User) method mirroring the confirmed
 * UserRoleRepository.findByFkUser(User) pattern. Both should be
 * verified against the actual repository files before merging.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceGetPaymentStatus {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final OrderItemRepository orderItemRepository;

    /*
     * ================================================================
     * GET PAYMENT STATUS
     * ================================================================
     */
    @Transactional(readOnly = true)
    public PaymentStatusResponseDto getPaymentStatus(UUID paymentId) {

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

        User authenticatedUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        List<String> authorityNames = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        boolean isAdmin = authorityNames.contains("ADMIN") || authorityNames.contains("SUPER_ADMIN");
        boolean isVendor = authorityNames.contains("VENDOR");

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */

        // Role-based authorization: customer must own the payment; vendor must
        // have at least one item on the linked order; admin bypasses both checks.
        if (isAdmin) {
            // Full access, no further check.
        } else if (isVendor) {
            Vendor vendor = vendorRepository.findByFkUser(authenticatedUser)
                    .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Vendor profile not found for authenticated user"));

            List<UUID> vendorOrderIds = orderItemRepository.findDistinctOrderIdsByVendorId(vendor.getPkVendorId());
            if (!vendorOrderIds.contains(payment.getFkOrder().getPkOrderId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to access this payment");
            }
        } else {
            // Default: customer role
            if (!payment.getFkUser().getPkUserId().equals(authenticatedUser.getPkUserId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to access this payment");
            }
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Note: Read-only lookup, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        // PCI masking: never expose the raw payment_method column value beyond
        // a generic display string. "Card ending in XXXX" requires the last
        // four digits of the underlying card, which are not present on the
        // Payment entity itself (they will live on PaymentMethod,).
        // Until, fall back to a generic gateway-based display label.
        String paymentMethodDisplay = maskPaymentMethodForDisplay(payment.getPaymentMethod());

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return PaymentStatusResponseDto.builder()
                .paymentId(payment.getPkPaymentId().toString())
                .orderId(payment.getFkOrder().getPkOrderId().toString())
                .status(payment.getFkStatus().getStatusName())
                .amountInPaise(payment.getAmountInPaise())
                .currency(payment.getCurrency())
                .paymentMethodDisplay(paymentMethodDisplay)
                .paymentGateway(payment.getFkGateway().getGatewayName())
                .createdAtUtc(payment.getCreatedAtUtc())
                .capturedAtUtc(payment.getCapturedAtUtc())
                .failureReason(payment.getFailureReason())
                .build();
    }

    /*
     * TODO: Replace with a real "Card ending in XXXX" lookup once
     * PaymentMethod.java is available and fkPaymentSourceId can be
     * resolved to a card_last_four value. Generic label only for now.
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
