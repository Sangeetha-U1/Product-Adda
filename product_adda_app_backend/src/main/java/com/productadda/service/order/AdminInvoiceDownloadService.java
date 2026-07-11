package com.productadda.service.order;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.order.AdminInvoiceDownloadResponseDto;

import com.productadda.entity.Invoice;
import com.productadda.entity.InvoicePresignedUrl;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.InvoiceRepository;
import com.productadda.repository.InvoicePresignedUrlRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminInvoiceDownloadService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoicePresignedUrlRepository invoicePresignedUrlRepository;
    private final InvoiceStorageService invoiceStorageService;

    /*
     * ================================================================
     * GENERATE ADMIN INVOICE DOWNLOAD URL (WITH ANTI-SPAM DB CACHE)
     * Description: Evaluates database tracking history for valid unexpired
     * presigned asset paths. Generates a new signature only upon expiration.
     * ================================================================
     */
    @Transactional
    public AdminInvoiceDownloadResponseDto generateDownloadUrl(UUID invoiceId) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */
        if (invoiceId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Target invoice identification key is required");
        }

        // ==========================================
        // 1.1 CONTEXT AUTHENTICATION
        // ==========================================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
        }

        String currentUsername = authentication.getName();

        // ==========================================
        // 1.2 DATABASE LOOKUP VALIDATION (LEVEL-2 ZERO TRUST)
        // ==========================================
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Authenticated user no longer exists"));

        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);
        if (userRoles.isEmpty()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required to access this endpoint");
        }

        boolean hasAdminPrivileges = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .anyMatch(roleName -> "ADMIN".equals(roleName) || "SUPER_ADMIN".equals(roleName));

        if (!hasAdminPrivileges) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required to access this endpoint");
        }

        // ==========================================
        // 1.3 ASSET EXISTENCE & STATUS GUARD
        // ==========================================
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invoice record not found"));

        if (!Boolean.TRUE.equals(invoice.getIsActive())) {
            throw new ApiException(HttpStatus.GONE, "Target invoice asset context is disabled or historical");
        }

        /*
         * ================================================================
         * 2. ANTI-SPAM CACHE EVALUATION WORKFLOW
         * ================================================================
         */
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

        Optional<InvoicePresignedUrl> cachedRecordOpt = invoicePresignedUrlRepository
                .findByFkInvoice_PkInvoiceIdAndIsActiveTrue(invoiceId);

        if (cachedRecordOpt.isPresent()) {
            InvoicePresignedUrl cachedRecord = cachedRecordOpt.get();

            // Check if cached token is still alive and secure
            if (cachedRecord.getExpiresAtUtc().isAfter(nowUtc)) {
                return AdminInvoiceDownloadResponseDto.builder()
                        .invoiceId(invoice.getPkInvoiceId())
                        .invoiceNumber(invoice.getInvoiceNumber())
                        .orderNumber(invoice.getFkOrder() != null ? invoice.getFkOrder().getOrderNumber() : "UNKNOWN")
                        .presignedUrl(cachedRecord.getPresignedUrl())
                        .urlExpirationUtc(cachedRecord.getExpiresAtUtc())
                        .build();
            } else {
                // Token is expired -> Soft delete by turning active flag off
                cachedRecord.setIsActive(false);
                cachedRecord.setUpdatedAtUtc(nowUtc);
                invoicePresignedUrlRepository.save(cachedRecord);
            }
        }

        /*
         * ================================================================
         * 3. CACHE MISS / RENEWAL WORKFLOW
         * ================================================================
         */
        // Generate a brand new link asset sequence valid for 15 minutes
        String freshPresignedUrl = invoiceStorageService.generatePresignedUrl(
                invoice.getObjectKey(),
                Duration.ofMinutes(15));

        LocalDateTime freshExpirationUtc = nowUtc.plusMinutes(15);

        // Write the active entry into tracking table history
        InvoicePresignedUrl newCacheEntry = InvoicePresignedUrl.builder()
                .pkPresignedUrlId(UUID.randomUUID())
                .fkInvoice(invoice)
                .presignedUrl(freshPresignedUrl)
                .expiresAtUtc(freshExpirationUtc)
                .isActive(true)
                .createdAtUtc(nowUtc)
                .updatedAtUtc(nowUtc)
                .build();

        invoicePresignedUrlRepository.save(newCacheEntry);

        /*
         * ================================================================
         * 4. RESPONSE MAPPING
         * ================================================================
         */
        return AdminInvoiceDownloadResponseDto.builder()
                .invoiceId(invoice.getPkInvoiceId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .orderNumber(invoice.getFkOrder() != null ? invoice.getFkOrder().getOrderNumber() : "UNKNOWN")
                .presignedUrl(freshPresignedUrl)
                .urlExpirationUtc(freshExpirationUtc)
                .build();
    }
}