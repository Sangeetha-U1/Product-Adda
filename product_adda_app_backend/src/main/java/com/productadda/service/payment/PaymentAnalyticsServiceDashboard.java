package com.productadda.service.payment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.payment.PaymentAnalyticsResponseDto;
import com.productadda.dto.payment.PaymentGatewayBreakdownDto;
import com.productadda.dto.payment.PaymentMethodBreakdownDto;
import com.productadda.dto.payment.RevenueTrendPointDto;
import com.productadda.exception.ApiException;
import com.productadda.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * PaymentAnalyticsServiceDashboard
 * GET /api/admin/analytics/payments
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class PaymentAnalyticsServiceDashboard {

    private final PaymentRepository paymentRepository;

    private static final List<String> REFUND_STATUS_NAMES = List.of("REFUNDED", "PARTIALLY_REFUNDED");

    /*
     * ================================================================
     * GET PAYMENT ANALYTICS
     * ================================================================
     */
    @Transactional(readOnly = true)
    public PaymentAnalyticsResponseDto getPaymentAnalytics(
            String startDateParam, String endDateParam, String paymentGateway, String paymentMethod) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        LocalDateTime startDate;
        if (startDateParam != null && !startDateParam.trim().isEmpty()) {
            try {
                startDate = LocalDate.parse(startDateParam.trim()).atStartOfDay();
            } catch (DateTimeParseException exception) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "startDate must be a valid ISO date (yyyy-MM-dd)");
            }
        } else {
            // Default: last 30 days, per the plan
            startDate = LocalDateTime.now().minusDays(30);
        }

        LocalDateTime endDate;
        if (endDateParam != null && !endDateParam.trim().isEmpty()) {
            try {
                endDate = LocalDate.parse(endDateParam.trim()).atTime(23, 59, 59);
            } catch (DateTimeParseException exception) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "endDate must be a valid ISO date (yyyy-MM-dd)");
            }
        } else {
            endDate = LocalDateTime.now();
        }

        if (startDate.isAfter(endDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "startDate must be before endDate");
        }

        String safeGateway = (paymentGateway == null || paymentGateway.trim().isEmpty())
                ? null
                : paymentGateway.trim().toUpperCase();

        String safeMethod = (paymentMethod == null || paymentMethod.trim().isEmpty())
                ? null
                : paymentMethod.trim().toLowerCase();

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        // Note: @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')") on the
        // controller already gates this endpoint - no further check needed here.

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        // Note: nothing to pre-fetch beyond the aggregate queries below.

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        long totalTransactions = paymentRepository.countTransactions(startDate, endDate, safeGateway, safeMethod);

        // CONFIRMED real status value: "SUCCESS" (not "SUCCESSFUL" as the
        // original plan draft assumed).
        long totalRevenueInPaise = paymentRepository.sumAmountByStatus(startDate, endDate, "SUCCESS", safeGateway, safeMethod);
        long totalRefundedInPaise = paymentRepository.sumAmountByStatusIn(startDate, endDate, REFUND_STATUS_NAMES, safeGateway, safeMethod);
        long netRevenueInPaise = totalRevenueInPaise - totalRefundedInPaise;

        long successCount = paymentRepository.countTransactionsByStatus(startDate, endDate, "SUCCESS", safeGateway, safeMethod);
        long failedCount = paymentRepository.countTransactionsByStatus(startDate, endDate, "FAILED", safeGateway, safeMethod);

        double successRatePercent = totalTransactions > 0
                ? Math.round((successCount * 10000.0) / totalTransactions) / 100.0
                : 0.0;
        double failedRatePercent = totalTransactions > 0
                ? Math.round((failedCount * 10000.0) / totalTransactions) / 100.0
                : 0.0;

        List<Object[]> methodBreakdownRaw = paymentRepository.getMethodBreakdown(startDate, endDate, safeGateway);
        List<PaymentMethodBreakdownDto> methodBreakdown = new ArrayList<>();
        for (Object[] row : methodBreakdownRaw) {
            methodBreakdown.add(PaymentMethodBreakdownDto.builder()
                    .paymentMethod((String) row[0])
                    .count((Long) row[1])
                    .revenueInPaise((Long) row[2])
                    .build());
        }

        List<Object[]> gatewayBreakdownRaw = paymentRepository.getGatewayBreakdown(startDate, endDate, safeMethod);
        List<PaymentGatewayBreakdownDto> gatewayBreakdown = new ArrayList<>();
        for (Object[] row : gatewayBreakdownRaw) {
            gatewayBreakdown.add(PaymentGatewayBreakdownDto.builder()
                    .paymentGateway((String) row[0])
                    .count((Long) row[1])
                    .revenueInPaise((Long) row[2])
                    .build());
        }

        List<Object[]> trendRaw = paymentRepository.getRevenueTrendByDay(startDate, endDate, safeGateway, safeMethod);
        List<RevenueTrendPointDto> revenueTrend = new ArrayList<>();
        for (Object[] row : trendRaw) {
            revenueTrend.add(RevenueTrendPointDto.builder()
                    .date(row[0].toString())
                    .revenueInPaise((Long) row[1])
                    .build());
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Note: Read-only aggregate lookup, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. RESPONSE MAPPING
         * ================================================================
         */
        return PaymentAnalyticsResponseDto.builder()
                .totalTransactions(totalTransactions)
                .totalRevenueInPaise(totalRevenueInPaise)
                .totalRefundedInPaise(totalRefundedInPaise)
                .netRevenueInPaise(netRevenueInPaise)
                .successRatePercent(successRatePercent)
                .failedRatePercent(failedRatePercent)
                .methodBreakdown(methodBreakdown)
                .gatewayBreakdown(gatewayBreakdown)
                .revenueTrend(revenueTrend)
                .build();
    }
}
