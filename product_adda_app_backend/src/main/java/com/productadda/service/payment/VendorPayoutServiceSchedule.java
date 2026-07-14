package com.productadda.service.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.payment.VendorPayoutResponseDto;
import com.productadda.dto.payment.VendorPayoutScheduleRequestDto;

import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;
import com.productadda.entity.PaymentAuditLog;
import com.productadda.entity.PaymentStatus;
import com.productadda.entity.Refund;
import com.productadda.entity.User;
import com.productadda.entity.Vendor;
import com.productadda.entity.VendorPayout;

import com.productadda.exception.ApiException;

import com.productadda.repository.PaymentAuditLogRepository;
import com.productadda.repository.PaymentStatusRepository;
import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.RefundRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.VendorPayoutRepository;
import com.productadda.repository.VendorRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * VendorPayoutServiceSchedule
 * POST /api/vendor-payouts/schedule
 *
 * IMPORTANT: this depends on DeliveryAssignment.deliveredAtUtc, a
 * brand-new column not currently SET by any existing service
 * Until that cross-module wiring happens, this
 * service will run successfully but always find zero eligible
 * orders for every vendor.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class VendorPayoutServiceSchedule {

    private final VendorRepository vendorRepository;
    private final VendorPayoutRepository vendorPayoutRepository;
    private final OrderItemRepository orderItemRepository;
    private final RefundRepository refundRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final PaymentAuditLogRepository paymentAuditLogRepository;
    private final UserRepository userRepository;

    private final UuidUtil uuidUtil;

    @Value("${payment.return-window-days:30}")
    private int returnWindowDays;

    @Value("${payment.default-commission-rate:10.00}")
    private BigDecimal defaultCommissionRate;

    private static final int PAYOUT_SCHEDULE_BUSINESS_DAYS = 5;

    /*
     * ================================================================
     * SCHEDULE VENDOR PAYOUTS
     * ================================================================
     */
    @Transactional
    public List<VendorPayoutResponseDto> scheduleVendorPayouts(VendorPayoutScheduleRequestDto request) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        UUID filterVendorId = null;

        if (request != null && request.getFilterVendorId() != null && !request.getFilterVendorId().trim().isEmpty()) {
            try {
                filterVendorId = UUID.fromString(request.getFilterVendorId());
            } catch (IllegalArgumentException exception) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "filterVendorId is not a valid identifier");
            }
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        // Note: @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')") on the
        // controller already gates this endpoint - resolving here only to
        // record the acting admin on each payout's audit log entry.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User actingAdmin = null;

        if (authentication != null && authentication.isAuthenticated()) {
            String email;
            if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                email = userDetails.getUsername();
            } else {
                email = authentication.getName();
            }
            actingAdmin = userRepository.findByEmail(email).orElse(null);
        }

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        List<Vendor> vendorsToProcess;
        
        if (filterVendorId != null) {

            // UUID finalFilterVendorId = filterVendorId;

            Vendor vendor = vendorRepository.findById(filterVendorId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vendor not found"));
            vendorsToProcess = List.of(vendor);

        } else {
            vendorsToProcess = vendorRepository.findAll().stream()
                    .filter(vendor -> Boolean.TRUE.equals(vendor.getIsActive()))
                    .toList();
        }

        PaymentStatus scheduledStatus = paymentStatusRepository.findByStatusName("SCHEDULED")
                .orElseThrow(
                        () -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "SCHEDULED status not configured"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime eligibleBeforeUtc = nowUtc.minusDays(returnWindowDays);

        List<VendorPayoutResponseDto> scheduledPayouts = new ArrayList<>();

        for (Vendor vendor : vendorsToProcess) {

            LocalDateTime sinceUtc = vendorPayoutRepository.findTopByFkVendorOrderByPeriodEndDateDesc(vendor)
                    .map(previousPayout -> previousPayout.getPeriodEndDate().atStartOfDay())
                    .orElse(LocalDateTime.of(1970, 1, 1, 0, 0));

            List<OrderItem> eligibleItems = orderItemRepository.findEligibleOrderItemsForPayout(
                    vendor.getPkVendorId(), sinceUtc, eligibleBeforeUtc);

            if (eligibleItems.isEmpty()) {
                continue;
            }

            // Exclude any order that has an active (in-flight) refund - money
            // still contested should not be paid out yet.
            Set<UUID> blockedOrderIds = new HashSet<>();
            Set<UUID> candidateOrderIds = new HashSet<>();
            for (OrderItem item : eligibleItems) {
                candidateOrderIds.add(item.getFkOrder().getPkOrderId());
            }
            for (UUID orderId : candidateOrderIds) {
                Order order = eligibleItems.stream()
                        .filter(item -> item.getFkOrder().getPkOrderId().equals(orderId))
                        .findFirst()
                        .map(OrderItem::getFkOrder)
                        .orElse(null);
                if (order == null) {
                    continue;
                }
                List<Refund> refundsOnOrder = refundRepository.findByFkOrder(order);
                boolean hasActiveRefund = refundsOnOrder.stream()
                        .anyMatch(refund -> "INITIATED".equalsIgnoreCase(refund.getFkStatus().getStatusName())
                                || "PENDING".equalsIgnoreCase(refund.getFkStatus().getStatusName()));
                if (hasActiveRefund) {
                    blockedOrderIds.add(orderId);
                }
            }

            List<OrderItem> payableItems = eligibleItems.stream()
                    .filter(item -> !blockedOrderIds.contains(item.getFkOrder().getPkOrderId()))
                    .toList();

            if (payableItems.isEmpty()) {
                continue;
            }

            // Sum line totals (rupees) -> paise
            BigDecimal totalAmountRupees = payableItems.stream()
                    .map(OrderItem::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long totalAmountInPaise = totalAmountRupees.multiply(BigDecimal.valueOf(100)).longValue();

            BigDecimal effectiveCommissionRate = vendor.getCommissionRate() != null
                    ? vendor.getCommissionRate()
                    : defaultCommissionRate;

            long platformCommissionInPaise = BigDecimal.valueOf(totalAmountInPaise)
                    .multiply(effectiveCommissionRate)
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                    .longValue();

            long netPayoutAmountInPaise = totalAmountInPaise - platformCommissionInPaise;

            // Determine period_start_date / period_end_date from the delivered
            // dates of the distinct orders included in this payout.
            LocalDate periodStartDate = null;

            LocalDate periodEndDate = null;

            Map<UUID, LocalDateTime> orderDeliveredDates = new HashMap<>();

            for (OrderItem item : payableItems) {
                Order order = item.getFkOrder();

                if (order.getDeliveredAtUtc() != null) {
                    orderDeliveredDates.put(
                            order.getPkOrderId(),
                            order.getDeliveredAtUtc());
                }
            }

            for (LocalDateTime deliveredAt : orderDeliveredDates.values()) {
                LocalDate deliveredDate = deliveredAt.toLocalDate();
                if (periodStartDate == null || deliveredDate.isBefore(periodStartDate)) {
                    periodStartDate = deliveredDate;
                }
                if (periodEndDate == null || deliveredDate.isAfter(periodEndDate)) {
                    periodEndDate = deliveredDate;
                }
            }

            if (periodStartDate == null) {
                periodStartDate = nowUtc.toLocalDate();
            }

            if (periodEndDate == null) {
                periodEndDate = nowUtc.toLocalDate();
            }

            LocalDate scheduledPayoutDate = addBusinessDays(nowUtc.toLocalDate(), PAYOUT_SCHEDULE_BUSINESS_DAYS);

            VendorPayout vendorPayout = VendorPayout.builder()
                    .pkVendorPayoutId(uuidUtil.generateUuidV7())
                    .fkVendor(vendor)
                    .fkStatus(scheduledStatus)
                    .periodStartDate(periodStartDate)
                    .periodEndDate(periodEndDate)
                    .totalAmountInPaise(totalAmountInPaise)
                    .platformCommissionInPaise(platformCommissionInPaise)
                    .netPayoutAmountInPaise(netPayoutAmountInPaise)
                    .scheduledPayoutDate(scheduledPayoutDate)
                    .build();

            /*
             * ================================================================
             * 3. DB SAVING SECTION
             * ================================================================
             */
            vendorPayout = vendorPayoutRepository.save(vendorPayout);

            Map<String, Object> metadata = new HashMap<>();

            metadata.put("vendorId", vendor.getPkVendorId().toString());
            metadata.put("netPayoutAmountInPaise", netPayoutAmountInPaise);

            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .pkAuditLogId(uuidUtil.generateUuidV7())
                    .fkActor(actingAdmin)
                    .actorRole(actingAdmin != null ? "ADMIN" : "SYSTEM")
                    .action("payout_scheduled")
                    .newStatus("SCHEDULED")
                    .metadata(metadata)
                    .build();

            paymentAuditLogRepository.save(auditLog);

            scheduledPayouts.add(VendorPayoutResponseDto.builder()
                    .id(vendorPayout.getPkVendorPayoutId().toString())
                    .vendorId(vendor.getPkVendorId().toString())
                    .vendorBusinessName(vendor.getBusinessName())
                    .periodStartDate(vendorPayout.getPeriodStartDate())
                    .periodEndDate(vendorPayout.getPeriodEndDate())
                    .totalAmountInPaise(totalAmountInPaise)
                    .platformCommissionInPaise(platformCommissionInPaise)
                    .netPayoutAmountInPaise(netPayoutAmountInPaise)
                    .status("SCHEDULED")
                    .scheduledPayoutDate(scheduledPayoutDate)
                    .createdAtUtc(vendorPayout.getCreatedAtUtc())
                    .build());
        }

        // TODO: will trigger payout_scheduled notification hooks per vendor

        /*
         * ================================================================
         * 4. RESPONSE MAPPING
         * ================================================================
         */
        return scheduledPayouts;
    }

    /*
     * Adds N business days (skipping Saturday/Sunday) to a date. No public
     * holiday calendar is consulted - a known simplification.
     */
    private LocalDate addBusinessDays(LocalDate startDate, int businessDaysToAdd) {
        LocalDate result = startDate;
        int addedDays = 0;
        while (addedDays < businessDaysToAdd) {
            result = result.plusDays(1);
            DayOfWeek dayOfWeek = result.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                addedDays++;
            }
        }
        return result;
    }
}
