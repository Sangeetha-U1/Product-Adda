package com.productadda.controller.admin;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.admin.AdminInvoiceDownloadResponseDto;
import com.productadda.dto.admin.PaginatedAdminInvoiceResponseDto;
import com.productadda.dto.order.InvoiceResponseDto;
import com.productadda.service.admin.AdminInvoiceDownloadService;
import com.productadda.service.admin.AdminInvoiceGenerationService;
import com.productadda.service.admin.AdminInvoicesRetrievalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminInvoiceController {

        private final AdminInvoiceGenerationService adminInvoiceGenerationService;
        private final AdminInvoicesRetrievalService adminInvoicesRetrievalService;
        private final AdminInvoiceDownloadService adminInvoiceDownloadService;

        // ==========================================
        // GET ALL SYSTEM INVOICES (PAGINATED)
        // Description: Grants comprehensive audit tracking access over complete
        // historical
        // global system invoice records exclusively for authorized system
        // administrators.
        // Clean Layering: Offloaded cleanly to AdminInvoicesRetrievalService.
        // ==========================================
        @GetMapping("/api/admin/orders/invoices")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<PaginatedAdminInvoiceResponseDto>> getAdminInvoices(
                        @PageableDefault(size = 10, page = 0) Pageable pageable) {

                // Delegates pagination extraction execution downstream safely
                PaginatedAdminInvoiceResponseDto data = adminInvoicesRetrievalService.getAdminInvoices(pageable);

                return ResponseEntity.status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<PaginatedAdminInvoiceResponseDto>builder()
                                                .success(true)
                                                .message("Global system invoice records loaded successfully.")
                                                .data(data)
                                                .build());
        }

        // ==========================================
        // UNIFIED ADMIN INVOICE GENERATE ENDPOINT
        // Description: Triggers initial compilation or checks modification
        // hashes to perform conditional updates.
        // Clean Layering: Handled cleanly via InvoiceGenerationService.
        // ==========================================
        @PostMapping("/api/admin/orders/{orderId}/invoices/generate")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<InvoiceResponseDto>> adminGenerateInvoice(
                        @PathVariable UUID orderId) {

                InvoiceResponseDto response = adminInvoiceGenerationService.adminGenerateInvoice(orderId);

                String message = (response.getInvoiceVersion() == 1)
                                ? "Invoice compiled successfully. Version 1"
                                : "Invoice details updated successfully. Version " + response.getInvoiceVersion();

                return ResponseEntity.status(HttpStatus.OK).body(ApiSuccessResponseDto.<InvoiceResponseDto>builder()
                                .success(true)
                                .message(message)
                                .data(response)
                                .build());
        }

        // ==========================================
        // ADMIN INVOICE DOWNLOAD LINK GENERATOR (JSON RESPONSE)
        // Description: Securely pulls back-end asset object keys and provides global
        // system
        // audit access to short-lived file presigned paths bypassing individual tenancy
        // restrictions.
        // Clean Layering: Offloaded cleanly to AdminInvoiceDownloadService.
        // ==========================================
        @GetMapping("/api/admin/orders/invoices/{invoiceId}/download")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<AdminInvoiceDownloadResponseDto>> downloadInvoice(
                        @PathVariable UUID invoiceId) {

                AdminInvoiceDownloadResponseDto data = adminInvoiceDownloadService.generateDownloadUrl(invoiceId);

                return ResponseEntity.status(HttpStatus.OK).body(ApiSuccessResponseDto
                                .<AdminInvoiceDownloadResponseDto>builder()
                                .success(true)
                                .message("Presigned file reference URL secured successfully. Valid for 15 minutes.")
                                .data(data)
                                .build());
        }

}