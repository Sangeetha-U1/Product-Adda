package com.productadda.service.invoice;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.order.InvoiceResponseDto;
import com.productadda.dto.order.UserInvoiceSummaryDto;
import com.productadda.dto.order.UserInvoiceWrapperDto;

import com.productadda.entity.Invoice;
import com.productadda.entity.Order;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.InvoiceRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceQueryService {

        private final UserRepository userRepository;
        private final UserRoleRepository userRoleRepository;
        private final OrderRepository orderRepository;
        private final InvoiceRepository invoiceRepository;

        /*
         * ================================================================
         * GET INVOICE BY ORDER ID
         * Description: Securely fetches tracked invoice metadata linked to
         * a specific order system context. Enforces zero-trust cross-tenant
         * security checks ensuring customers can only access their own
         * generated invoice entries while letting administrators bypass ownership
         * bounds.
         * ================================================================
         */
        @Transactional(readOnly = true)
        public InvoiceResponseDto getInvoiceByOrderId(UUID orderId) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (orderId == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Order identity reference is required");
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

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order record not found"));

                List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);
                if (userRoles.isEmpty()) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "User lacks privileges to access this order metadata");
                }

                boolean isAdminOrSuperAdmin = userRoles.stream()
                                .map(role -> role.getFkRole().getRoleName())
                                .anyMatch(name -> "ADMIN".equals(name) || "SUPER_ADMIN".equals(name));

                // Symmetrical Authorization Check: Non-administrators must match the
                // exact user identity associated with the database order resource.
                if (!isAdminOrSuperAdmin && !order.getFkUser().getPkUserId().equals(currentUser.getPkUserId())) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Access denied to requested order invoice records");
                }

                Invoice invoice = invoiceRepository.findByFkOrder_PkOrderId(orderId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "No invoice exists for this order"));

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * Reason: Read-only operational GET queries bypass workflow calculations.
                 * ================================================================
                 */

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION (Skip if Read-Only GET)
                 * Reason: Read-only lookup, no mutations performed.
                 * ================================================================
                 */

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * ================================================================
                 */
                UUID responseInvoiceId = invoice.getPkInvoiceId();
                String responseInvoiceNumber = invoice.getInvoiceNumber();
                int responseInvoiceVersion = invoice.getInvoiceVersion();
                java.math.BigDecimal responseAmount = invoice.getInvoiceAmount();
                java.time.LocalDateTime responseGeneratedTime = invoice.getGeneratedAt();
                String responseFileUrl = invoice.getFileUrl();

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * ================================================================
                 */
                return InvoiceResponseDto.builder()
                                .invoiceId(responseInvoiceId)
                                .invoiceNumber(responseInvoiceNumber)
                                .invoiceVersion(responseInvoiceVersion)
                                .orderId(orderId)
                                .invoiceAmount(responseAmount)
                                .generatedAt(responseGeneratedTime)
                                .fileUrl(responseFileUrl)
                                // .presignedUrl(null)
                                .build();
        }

        /*
         * ================================================================
         * GET USER INVOICES (PAGINATED)
         * Description: Retrieves all active invoice records associated with the
         * currently authenticated user account. Enforces Level-2 Zero-Trust isolation
         * by verifying active database roles before querying records strictly tied
         * to the security context profile ID.
         * ================================================================
         */
        @Transactional(readOnly = true)
        public UserInvoiceWrapperDto getUserInvoices(org.springframework.data.domain.Pageable pageable) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (pageable == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Pagination processing configuration parameters are required");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                                .getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication context missing or invalid");
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
                        throw new ApiException(HttpStatus.FORBIDDEN, "User lacks access authorization configurations");
                }

                // Enforce security perimeter verification check: ensure the database contains a
                // functional
                // identity layout for the executing principal before initiating downstream
                // record scanning.
                boolean isAdminOrSuperAdmin = userRoles.stream()
                                .map(role -> role.getFkRole().getRoleName())
                                .anyMatch(name -> "ADMIN".equals(name) || "SUPER_ADMIN".equals(name));

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */

                // If the user is an admin or super admin, they can see all invoices pageable.
                // Otherwise, standard customers are rigidly bound to rows matching their user
                // ID.
                org.springframework.data.domain.Page<Invoice> invoicePage;
                if (isAdminOrSuperAdmin) {
                        invoicePage = invoiceRepository.findAll(pageable);
                } else {
                        invoicePage = invoiceRepository.findByFkOrder_FkUser_PkUserIdAndIsActiveTrue(
                                        currentUser.getPkUserId(),
                                        pageable);
                }

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * Reason: Read-only query method operation execution stream.
                 * ================================================================
                 */

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * Description: Safely maps rows to individual summaries and appends
                 * fresh short-lived transient presigned URLs.
                 * ================================================================
                 */
                List<UserInvoiceSummaryDto> invoiceSummaries = invoicePage.getContent().stream()
                                .map(invoice -> {
                                        return UserInvoiceSummaryDto.builder()
                                                        .invoiceId(invoice.getPkInvoiceId())
                                                        .invoiceNumber(invoice.getInvoiceNumber())
                                                        .invoiceVersion(invoice.getInvoiceVersion())
                                                        .orderId(invoice.getFkOrder().getPkOrderId())
                                                        .orderNumber(invoice.getFkOrder().getOrderNumber())
                                                        .invoiceAmount(invoice.getInvoiceAmount())
                                                        .generatedAt(invoice.getGeneratedAt())
                                                        .fileUrl(invoice.getFileUrl())
                                                        .build();
                                })
                                .toList();

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * ================================================================
                 */
                return UserInvoiceWrapperDto.builder()
                                .invoices(invoiceSummaries)
                                .pageNumber(invoicePage.getNumber())
                                .pageSize(invoicePage.getSize())
                                .totalElements(invoicePage.getTotalElements())
                                .totalPages(invoicePage.getTotalPages())
                                .isLastPage(invoicePage.isLast())
                                .build();
        }

}