package com.productadda.service.payment;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.payment.ReconciliationDiscrepancyDto;
import com.productadda.dto.payment.ReconciliationResultDto;
import com.productadda.dto.payment.ReconciliationTriggerRequestDto;
import com.productadda.entity.Payment;
import com.productadda.entity.PaymentAuditLog;
import com.productadda.entity.PaymentGateway;
import com.productadda.entity.PaymentReconciliationLog;
import com.productadda.entity.PaymentStatus;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.PaymentAuditLogRepository;
import com.productadda.repository.PaymentGatewayRepository;
import com.productadda.repository.PaymentRepository;
import com.productadda.repository.PaymentReconciliationLogRepository;
import com.productadda.repository.PaymentStatusRepository;
import com.productadda.repository.UserRepository;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * ReconciliationServiceDailyRun
 * POST /api/reconciliation/daily (manual admin trigger).
 * Also called directly by ScheduledReconciliationTask for the 2 AM
 * UTC automatic run - the two entry points share this one method
 * so the comparison logic only exists in one place.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class ReconciliationServiceDailyRun {

    private final PaymentRepository paymentRepository;
    private final PaymentAuditLogRepository paymentAuditLogRepository;
    private final PaymentReconciliationLogRepository paymentReconciliationLogRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final PaymentGatewayRepository paymentGatewayRepository;
    private final UserRepository userRepository;
    private final RazorpayGatewayFetchPaymentsService razorpayGatewayFetchPaymentsService;
    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * RUN RECONCILIATION - MANUAL ADMIN TRIGGER
     * ================================================================
     */
    @Transactional
    public ReconciliationResultDto runManualReconciliation(ReconciliationTriggerRequestDto request) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        LocalDateTime fromUtc;
        LocalDateTime toUtc;
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

        if (request != null && request.getPastDate() != null && !request.getPastDate().trim().isEmpty()) {
            try {
                fromUtc = LocalDateTime.parse(request.getPastDate().trim());
            } catch (DateTimeParseException exception) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "pastDate must be a valid ISO-8601 datetime");
            }
        } else {
            fromUtc = nowUtc.minusHours(24);
        }

        if (request != null && request.getTillDate() != null && !request.getTillDate().trim().isEmpty()) {
            try {
                toUtc = LocalDateTime.parse(request.getTillDate().trim());
            } catch (DateTimeParseException exception) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "tillDate must be a valid ISO-8601 datetime");
            }
        } else {
            toUtc = nowUtc;
        }

        if (!fromUtc.isBefore(toUtc)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "pastDate must be before tillDate");
        }
        if (toUtc.isAfter(nowUtc)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "tillDate must not be in the future");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        // Note: @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')") on the
        // controller already gates this endpoint - resolving the acting admin
        // here only to record it on the reconciliation log.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User triggeredByUser = null;
        if (authentication != null && authentication.isAuthenticated()) {
            String email;
            if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                email = userDetails.getUsername();
            } else {
                email = authentication.getName();
            }
            triggeredByUser = userRepository.findByEmail(email).orElse(null);
        }

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        // Note: nothing to pre-fetch here beyond the lookup tables used below.

        return runReconciliation(fromUtc, toUtc, "on_demand", triggeredByUser);
    }

    /*
     * ================================================================
     * RUN RECONCILIATION - SHARED CORE LOGIC
     * Called by both runManualReconciliation(...) above and
     * ScheduledReconciliationTask's 2 AM UTC scheduled run.
     * ================================================================
     */
    @Transactional
    public ReconciliationResultDto runReconciliation(
            LocalDateTime fromUtc, LocalDateTime toUtc, String reconciliationType, User triggeredByUser) {

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */

        // real payment_statuses table confirmed via
        // SELECT status_name FROM payment_statuses - "RUNNING" does NOT
        // exist. Reusing "INITIATED" (confirmed) for the pre-comparison
        // state instead.
        PaymentStatus runningStatus = paymentStatusRepository.findByStatusName("INITIATED")
                .orElseThrow(
                        () -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "INITIATED status not configured"));

        PaymentGateway razorpayGateway = paymentGatewayRepository.findByGatewayName("RAZORPAY")
                .orElseThrow(
                        () -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "RAZORPAY gateway not configured"));

        UUID reconciliationRunId = uuidUtil.generateUuidV7();

        PaymentReconciliationLog reconciliationLog = PaymentReconciliationLog.builder()
                .pkReconciliationLogId(uuidUtil.generateUuidV7())
                .reconciliationType(reconciliationType)
                .reconciliationRunId(reconciliationRunId)
                .fkGateway(razorpayGateway)
                .fkStatus(runningStatus)
                .fkTriggeredByUser(triggeredByUser)
                .totalGatewayTransactions(0)
                .totalDbRecords(0)
                .matchedRecords(0)
                .missingInDb(0)
                .orphanedInDb(0)
                .build();
        reconciliationLog = paymentReconciliationLogRepository.save(reconciliationLog);

        // Fetch both sides of the comparison.
        List<RazorpayGatewayFetchPaymentsService.RazorpayTransactionSummary> gatewayTransactions = razorpayGatewayFetchPaymentsService
                .fetchPayments(fromUtc, toUtc);
        List<Payment> localPayments = paymentRepository.findByCreatedAtUtcBetween(fromUtc, toUtc);

        Set<String> gatewayTransactionIds = new HashSet<>();
        for (RazorpayGatewayFetchPaymentsService.RazorpayTransactionSummary transaction : gatewayTransactions) {
            gatewayTransactionIds.add(transaction.getGatewayTransactionId());
        }

        Set<String> localGatewayTransactionIds = new HashSet<>();
        for (Payment payment : localPayments) {
            if (payment.getGatewayTransactionId() != null) {
                localGatewayTransactionIds.add(payment.getGatewayTransactionId());
            }
        }

        List<ReconciliationDiscrepancyDto> missingInDb = new ArrayList<>();
        int matchedCount = 0;
        for (RazorpayGatewayFetchPaymentsService.RazorpayTransactionSummary transaction : gatewayTransactions) {
            if (localGatewayTransactionIds.contains(transaction.getGatewayTransactionId())) {
                matchedCount++;
            } else {
                missingInDb.add(ReconciliationDiscrepancyDto.builder()
                        .gatewayTransactionId(transaction.getGatewayTransactionId())
                        .amountInPaise(transaction.getAmountInPaise())
                        .paymentId(null)
                        .build());
            }
        }

        List<ReconciliationDiscrepancyDto> orphanedInDb = new ArrayList<>();
        for (Payment payment : localPayments) {
            if (payment.getGatewayTransactionId() == null
                    || !gatewayTransactionIds.contains(payment.getGatewayTransactionId())) {
                orphanedInDb.add(ReconciliationDiscrepancyDto.builder()
                        .gatewayTransactionId(payment.getGatewayTransactionId())
                        .amountInPaise(payment.getAmountInPaise())
                        .paymentId(payment.getPkPaymentId().toString())
                        .build());
            }
        }

        boolean hasDiscrepancies = !missingInDb.isEmpty() || !orphanedInDb.isEmpty();

        // "COMPLETED_WITH_DISCREPANCIES" does not exist
        // in the real payment_statuses table. "FAILED_WITH_ERRORS" DOES
        // exist (confirmed via SELECT status_name FROM payment_statuses) -
        // this was actually correct in the original seed script and
        // was wrongly "corrected" away. Reverted here.
        String finalStatusName = hasDiscrepancies ? "FAILED_WITH_ERRORS" : "COMPLETED";
        PaymentStatus finalStatus = paymentStatusRepository.findByStatusName(finalStatusName)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        finalStatusName + " status not configured"));

        JSONObject discrepancyJson = new JSONObject();
        JSONArray missingJsonArray = new JSONArray();
        for (ReconciliationDiscrepancyDto discrepancy : missingInDb) {
            JSONObject entry = new JSONObject();
            entry.put("gatewayTransactionId", discrepancy.getGatewayTransactionId());
            entry.put("amountInPaise", discrepancy.getAmountInPaise());
            missingJsonArray.put(entry);
        }
        JSONArray orphanedJsonArray = new JSONArray();
        for (ReconciliationDiscrepancyDto discrepancy : orphanedInDb) {
            JSONObject entry = new JSONObject();
            entry.put("paymentId", discrepancy.getPaymentId());
            entry.put("gatewayTransactionId", discrepancy.getGatewayTransactionId());
            orphanedJsonArray.put(entry);
        }
        discrepancyJson.put("missing_in_db", missingJsonArray);
        discrepancyJson.put("orphaned_in_db", orphanedJsonArray);

        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

        reconciliationLog.setTotalGatewayTransactions(gatewayTransactions.size());
        reconciliationLog.setTotalDbRecords(localPayments.size());
        reconciliationLog.setMatchedRecords(matchedCount);
        reconciliationLog.setMissingInDb(missingInDb.size());
        reconciliationLog.setOrphanedInDb(orphanedInDb.size());
        reconciliationLog.setDiscrepancyDetails(discrepancyJson.toString());
        reconciliationLog.setFkStatus(finalStatus);
        reconciliationLog.setReconciledAtUtc(nowUtc);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        reconciliationLog = paymentReconciliationLogRepository.save(reconciliationLog);

        Map<String, Object> detailsMap = new HashMap<>();

        detailsMap.put("reconciliationLogId", reconciliationLog.getPkReconciliationLogId().toString());
        detailsMap.put("matchedRecords", matchedCount);
        detailsMap.put("discrepanciesCount", missingInDb.size() + orphanedInDb.size());

        // Note: PaymentAuditLog has no dedicated reconciliation-log FK column
        // in the current schema - the reconciliation_log_id is embedded in
        // the JSON details payload instead of a formal relationship.

        PaymentAuditLog auditLog = PaymentAuditLog.builder()
                .pkAuditLogId(uuidUtil.generateUuidV7())
                .action("reconciliation_completed")
                .fkActor(triggeredByUser)
                .actorRole(triggeredByUser != null ? "ADMIN" : "SYSTEM")
                .oldStatus("INITIATED")
                .newStatus(finalStatusName)
                .metadata(detailsMap)
                .build();
                
        paymentAuditLogRepository.save(auditLog);

        // TODO: if hasDiscrepancies, trigger an incident alert for manual
        // review (notification service hook, not yet wired).

        /*
         * ================================================================
         * 4. RESPONSE MAPPING
         * ================================================================
         */
        String message = hasDiscrepancies
                ? (missingInDb.size() + orphanedInDb.size()) + " discrepancy(ies) require manual review"
                : "Perfect reconciliation";

        return ReconciliationResultDto.builder()
                .reconciliationLogId(reconciliationLog.getPkReconciliationLogId().toString())
                .reconciliationRunId(reconciliationRunId.toString())
                .status(finalStatusName)
                .totalGatewayTransactions(gatewayTransactions.size())
                .totalDbRecords(localPayments.size())
                .matchedRecords(matchedCount)
                .missingInDb(missingInDb)
                .orphanedInDb(orphanedInDb)
                .reconciledAtUtc(nowUtc)
                .message(message)
                .build();
    }
}
