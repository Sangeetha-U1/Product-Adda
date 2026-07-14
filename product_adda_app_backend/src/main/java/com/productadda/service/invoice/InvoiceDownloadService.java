package com.productadda.service.invoice;

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

import com.productadda.dto.order.InvoiceDownloadResponseDto;
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
public class InvoiceDownloadService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoicePresignedUrlRepository cacheRepository;
    private final InvoiceStorageService invoiceStorageService;

    /*
     * ================================================================
     * FETCH INVOICE DOWNLOAD PRESIGNED URL (WITH ANTI-SPAM DB CACHE)
     * Description: Secures short-lived cloud download links for an invoice file
     * asset. Verifies cross-tenant boundaries and checks the shared 
     * database cache table for active, unexpired presigned URLs before 
     * requesting a new cryptographic signature.
     * ================================================================
     */
    @Transactional
    public InvoiceDownloadResponseDto invoiceDownload(UUID invoiceId) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (invoiceId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invoice identity reference is required");
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

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invoice record not found"));

        // Soft-delete and lifecycle guard: block active generation workflows
        // targeting disabled or historical records.
        if (!Boolean.TRUE.equals(invoice.getIsActive())) {
            throw new ApiException(HttpStatus.GONE, "Target invoice asset context is disabled");
        }

        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);
        if (userRoles.isEmpty()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "User lacks access authorization configurations");
        }

        boolean isAdminOrSuperAdmin = userRoles.stream()
                .map(role -> role.getFkRole().getRoleName())
                .anyMatch(name -> "ADMIN".equals(name) || "SUPER_ADMIN".equals(name));

        // Strict Tenancy Verification: Non-administrative personnel can only request
        // URL compilation payloads targeting invoices anchored to their profile entity.
        if (!isAdminOrSuperAdmin && !invoice.getFkOrder().getFkUser().getPkUserId()
                .equals(currentUser.getPkUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied to download requested invoice file");
        }

        /*
         * ================================================================
         * 2. ANTI-SPAM CACHE EVALUATION WORKFLOW
         * ================================================================
         */
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        
        Optional<InvoicePresignedUrl> cachedRecordOpt = cacheRepository
                .findByFkInvoice_PkInvoiceIdAndIsActiveTrue(invoiceId);

        if (cachedRecordOpt.isPresent()) {
            InvoicePresignedUrl cachedRecord = cachedRecordOpt.get();

            // Evaluate if cached token is still alive and safe for re-use
            if (cachedRecord.getExpiresAtUtc().isAfter(nowUtc)) {
                return InvoiceDownloadResponseDto.builder()
                        .invoiceNumber(invoice.getInvoiceNumber())
                        .invoiceVersion(invoice.getInvoiceVersion())
                        .presignedUrl(cachedRecord.getPresignedUrl())
                        .urlExpirationUtc(cachedRecord.getExpiresAtUtc())
                        .build();
            } else {
                // Token is expired -> Soft delete by turning active flag off
                cachedRecord.setIsActive(false);
                cachedRecord.setUpdatedAtUtc(nowUtc);
                cacheRepository.save(cachedRecord);
            }
        }

        /*
         * ================================================================
         * 3. CACHE MISS / SIGNING GENERATION WORKFLOW
         * ================================================================
         */
        String securedObjectKey = invoice.getObjectKey();
        Duration validationExpiryWindow = Duration.ofMinutes(15);
        
        String transientUrl = invoiceStorageService.generatePresignedUrl(securedObjectKey,
                validationExpiryWindow);

        if (transientUrl == null || transientUrl.trim().isEmpty()) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to secure transient cloud access credential tokens");
        }

        LocalDateTime freshExpirationUtc = nowUtc.plusMinutes(15);

        // Commit active tracking footprint record to avoid calculation spamming
        InvoicePresignedUrl newCacheEntry = InvoicePresignedUrl.builder()
                .pkPresignedUrlId(UUID.randomUUID())
                .fkInvoice(invoice)
                .presignedUrl(transientUrl)
                .expiresAtUtc(freshExpirationUtc)
                .isActive(true)
                .createdAtUtc(nowUtc)
                .updatedAtUtc(nowUtc)
                .build();

        cacheRepository.save(newCacheEntry);

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return InvoiceDownloadResponseDto.builder()
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceVersion(invoice.getInvoiceVersion())
                .presignedUrl(transientUrl)
                .urlExpirationUtc(freshExpirationUtc)
                .build();
    }
}