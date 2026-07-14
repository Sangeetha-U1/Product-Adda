package com.productadda.service.payment;

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

import com.productadda.dto.payment.PaginatedPaymentResponseDto;
import com.productadda.dto.payment.PaymentListItemDto;

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
public class PaymentsGetAllPaymentsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PaymentRepository paymentRepository;

    /*
     * ================================================================
     * GET ALL PAYMENTS FOR AUTHENTICATED USER
     * Description: Paginated, filterable, searchable list of the
     * authenticated user's own payments. Mirrors OrderHistoryService's
     * auth pattern with the added UserRole existence check.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public PaginatedPaymentResponseDto getAllPayments(
            int page, int pageSize, String sortBy, String sortOrder,
            String status, String gatewayName, String startDate, String endDate,
            Long minAmountInPaise, Long maxAmountInPaise, String keyword) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        int safePage = page < 0 ? 0 : page;
        int safePageSize = (pageSize <= 0 || pageSize > 100) ? 20 : pageSize;

        String safeSortBy = (sortBy == null || sortBy.trim().isEmpty()) ? "createdAtUtc" : sortBy.trim();

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String safeStatus = (status == null || status.trim().isEmpty())
                ? null
                : status.trim().toUpperCase();

        String safeGatewayName = (gatewayName == null || gatewayName.trim().isEmpty())
                ? null
                : gatewayName.trim().toUpperCase();

        String safeKeyword = (keyword == null || keyword.trim().isEmpty())
                ? null
                : "%" + keyword.trim() + "%";

        if (minAmountInPaise != null && minAmountInPaise < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "minAmountInPaise cannot be negative");
        }
        if (maxAmountInPaise != null && maxAmountInPaise < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "maxAmountInPaise cannot be negative");
        }
        if (minAmountInPaise != null && maxAmountInPaise != null && minAmountInPaise > maxAmountInPaise) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "minAmountInPaise cannot be greater than maxAmountInPaise");
        }

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
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);
        if (userRoles.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User role not found");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        PageRequest pageRequest = PageRequest.of(safePage, safePageSize, Sort.by(direction, safeSortBy));

        Page<Payment> paymentPage;

        try {
            paymentPage = paymentRepository.findAllPaymentsForUserWithFiltersAndSearch(
                    currentUser, safeStatus, safeGatewayName, startDateTime, endDateTime,
                    minAmountInPaise, maxAmountInPaise, safeKeyword, pageRequest);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid sortBy field: " + safeSortBy);
        }

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
        List<PaymentListItemDto> items = paymentPage.getContent().stream()
                .map(payment -> PaymentListItemDto.builder()
                        .paymentId(payment.getPkPaymentId())
                        .orderId(payment.getFkOrder() != null ? payment.getFkOrder().getPkOrderId() : null)
                        .orderNumber(payment.getFkOrder() != null ? payment.getFkOrder().getOrderNumber() : null)
                        .statusName(payment.getFkStatus() != null ? payment.getFkStatus().getStatusName() : "UNKNOWN")
                        .paymentMethod(payment.getPaymentMethod())
                        .gatewayName(payment.getFkGateway() != null ? payment.getFkGateway().getGatewayName() : null)
                        .amountInPaise(payment.getAmountInPaise())
                        .currency(payment.getCurrency())
                        .createdAtUtc(payment.getCreatedAtUtc())
                        .paidAtUtc(payment.getPaidAtUtc())
                        .build())
                .collect(Collectors.toList());

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return PaginatedPaymentResponseDto.builder()
                .payments(items)
                .totalCount(paymentPage.getTotalElements())
                .page(safePage)
                .pageSize(safePageSize)
                .build();
    }
}