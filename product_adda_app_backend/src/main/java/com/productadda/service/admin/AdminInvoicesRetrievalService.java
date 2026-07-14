package com.productadda.service.admin;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.admin.AdminInvoiceDetailDto;
import com.productadda.dto.admin.PaginatedAdminInvoiceResponseDto;
import com.productadda.entity.Invoice;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.InvoiceRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminInvoicesRetrievalService {

        private final UserRepository userRepository;
        private final UserRoleRepository userRoleRepository;
        private final InvoiceRepository invoiceRepository;

        /*
         * ================================================================
         * GET ADMIN INVOICES (PAGINATED)
         * Description: Retrieves a full-visibility system-wide collection of all
         * invoice assets. Implements strict database-driven Level-2 Zero-Trust
         * verification to ensure active administrative access status.
         * ================================================================
         */
        @Transactional(readOnly = true)
        public PaginatedAdminInvoiceResponseDto getAdminInvoices(org.springframework.data.domain.Pageable pageable) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */
                if (pageable == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Pagination processing configuration parameters are required");
                }

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (pageable.getPageNumber() < 0) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "page must be >= 0");
                }
                if (pageable.getPageSize() <= 0 || pageable.getPageSize() > 100) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "size must be > 0 and <= 100");
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
                // 1.3 DATABASE LOOKUP VALIDATION (LEVEL-2 ZERO TRUST)
                // ==========================================
                User currentUser = userRepository.findByEmail(currentUsername)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user no longer exists"));

                List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);
                if (userRoles.isEmpty()) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Admin role required to access this endpoint");
                }

                boolean hasAdminPrivileges = userRoles.stream()
                                .map(userRole -> userRole.getFkRole().getRoleName())
                                .anyMatch(roleName -> "ADMIN".equals(roleName) || "SUPER_ADMIN".equals(roleName));

                if (!hasAdminPrivileges) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Admin role required to access this endpoint");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                // Merges pagination window details while forcing standard high-to-low timeline
                // sorting criteria
                org.springframework.data.domain.PageRequest sortedPageRequest = org.springframework.data.domain.PageRequest
                                .of(
                                                pageable.getPageNumber(),
                                                pageable.getPageSize(),
                                                org.springframework.data.domain.Sort.by(
                                                                org.springframework.data.domain.Sort.Direction.DESC,
                                                                "generatedAt"));

                Page<Invoice> invoicePage = invoiceRepository.findAll(sortedPageRequest);

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * ================================================================
                 */
                List<AdminInvoiceDetailDto> content = invoicePage.getContent().stream()
                                .map(invoice -> AdminInvoiceDetailDto.builder()
                                                .invoiceId(invoice.getPkInvoiceId())
                                                .invoiceNumber(invoice.getInvoiceNumber())
                                                .invoiceVersion(invoice.getInvoiceVersion())
                                                .orderId(invoice.getFkOrder() != null
                                                                ? invoice.getFkOrder().getPkOrderId()
                                                                : null)
                                                .orderNumber(invoice.getFkOrder() != null
                                                                ? invoice.getFkOrder().getOrderNumber()
                                                                : null)
                                                .customerId((invoice.getFkOrder() != null
                                                                && invoice.getFkOrder().getFkUser() != null)
                                                                                ? invoice.getFkOrder().getFkUser()
                                                                                                .getPkUserId()
                                                                                : null)
                                                .customerEmail((invoice.getFkOrder() != null
                                                                && invoice.getFkOrder().getFkUser() != null)
                                                                                ? invoice.getFkOrder().getFkUser()
                                                                                                .getEmail()
                                                                                : null)
                                                .invoiceAmount(invoice.getInvoiceAmount())
                                                .fileUrl(invoice.getFileUrl())
                                                .objectKey(invoice.getObjectKey())
                                                .generatedAt(invoice.getGeneratedAt())
                                                .isActive(invoice.getIsActive())
                                                .createdAtUtc(invoice.getCreatedAtUtc())
                                                .updatedAtUtc(invoice.getUpdatedAtUtc())
                                                .build())
                                .collect(Collectors.toList());

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * ================================================================
                 */
                return PaginatedAdminInvoiceResponseDto.builder()
                                .content(content)
                                .totalElements(invoicePage.getTotalElements())
                                .page(invoicePage.getNumber())
                                .size(invoicePage.getSize())
                                .build();
        }
}