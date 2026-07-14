package com.productadda.service.payment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.payment.RefundLineItemResponseDto;
import com.productadda.dto.payment.RefundResponseDto;
import com.productadda.entity.PaymentStatus;
import com.productadda.entity.Refund;
import com.productadda.entity.RefundLineItem;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.PaymentStatusRepository;
import com.productadda.repository.RefundLineItemRepository;
import com.productadda.repository.RefundRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * RefundServiceGetStatus
 * API 9/14 - GET /api/refunds/{refundId}
 * Role-based access: customer sees own refunds (via
 * Payment.fkCustomer); admin sees all. Optional live gateway sync
 * when status is still INITIATED/PROCESSING.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class RefundServiceGetStatus {

    private final RefundRepository refundRepository;
    private final RefundLineItemRepository refundLineItemRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final UserRepository userRepository;

    private final RazorpayGatewayFetchRefundStatusService razorpayGatewayFetchRefundStatusService;

    /*
     * ================================================================
     * GET REFUND STATUS
     * ================================================================
     */
    @Transactional
    public RefundResponseDto getRefundStatus(UUID refundId) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (refundId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "refundId is required");
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

        User authenticatedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ADMIN") || authority.equals("SUPER_ADMIN"));

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        Refund refund;
        if (isAdmin) {
            refund = refundRepository.findById(refundId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Refund not found"));
        } else {
            refund = refundRepository
                    .findByPkRefundIdAndFkPayment_FkCustomer_PkUserId(refundId, authenticatedUser.getPkUserId())
                    .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN,
                            "You do not have permission to view this refund"));
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */

        // Optional live sync: if the refund is still in-flight at the gateway,
        // check for a status update rather than trusting a possibly-stale
        // local row (covers the case where Razorpay's refund webhook, if any,
        // has not yet arrived).
        String localStatusName = refund.getFkStatus().getStatusName();
        boolean isInFlight = "INITIATED".equalsIgnoreCase(localStatusName)
                || "PROCESSING".equalsIgnoreCase(localStatusName);

        if (isInFlight && refund.getGatewayRefundId() != null) {
            String gatewayStatus = razorpayGatewayFetchRefundStatusService
                    .fetchRefundStatus(refund.getGatewayRefundId());

            // TODO: Razorpay refund statuses are "pending"/"processed"/"failed" -
            // mapped here to our own PROCESSING/SUCCESS/FAILED vocabulary.
            // Not verified against a real Razorpay refund response.
            if ("processed".equalsIgnoreCase(gatewayStatus)) {
                PaymentStatus successStatus = paymentStatusRepository.findByStatusName("SUCCESS")
                        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "SUCCESS status not configured"));
                refund.setFkStatus(successStatus);
                refund = refundRepository.save(refund);
            } else if ("failed".equalsIgnoreCase(gatewayStatus)) {
                PaymentStatus failedStatus = paymentStatusRepository.findByStatusName("FAILED")
                        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "FAILED status not configured"));
                refund.setFkStatus(failedStatus);
                refund = refundRepository.save(refund);
            }
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Note: only mutates when a live-sync status change occurred above;
         * otherwise this is a read-only lookup.
         * ================================================================
         */

        List<RefundLineItemResponseDto> lineItemResponseDtos = null;
        if ("partial".equalsIgnoreCase(refund.getRefundType())) {
            List<RefundLineItem> refundLineItems = refundLineItemRepository.findByFkRefund(refund);
            lineItemResponseDtos = new ArrayList<>();
            for (RefundLineItem refundLineItem : refundLineItems) {
                lineItemResponseDtos.add(RefundLineItemResponseDto.builder()
                        .orderItemId(refundLineItem.getFkOrderItem().getPkOrderItemId().toString())
                        .productNameSnapshot(refundLineItem.getFkOrderItem().getProductNameSnapshot())
                        .refundAmountInPaise(refundLineItem.getRefundAmountInPaise())
                        .build());
            }
        }

        /*
         * ================================================================
         * 4. RESPONSE MAPPING
         * ================================================================
         */
        return RefundResponseDto.builder()
                .refundId(refund.getPkRefundId().toString())
                .paymentId(refund.getFkPayment().getPkPaymentId().toString())
                .orderId(refund.getFkOrder().getPkOrderId().toString())
                .refundType(refund.getRefundType())
                .refundAmountInPaise(refund.getRefundAmountInPaise())
                .status(refund.getFkStatus().getStatusName())
                .gatewayRefundId(refund.getGatewayRefundId())
                .initiatedBy(refund.getInitiatedBy())
                .reason(refund.getReason())
                .createdAtUtc(refund.getCreatedAtUtc())
                .processedAtUtc(refund.getProcessedAtUtc())
                .failureReason(refund.getFailureReason())
                .lineItems(lineItemResponseDtos)
                .build();
    }
}
