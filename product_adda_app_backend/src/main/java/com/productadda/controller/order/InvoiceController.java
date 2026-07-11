package com.productadda.controller.order;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.order.InvoiceDownloadResponseDto;
import com.productadda.dto.order.InvoiceResponseDto;
import com.productadda.dto.order.UserInvoiceWrapperDto;
import com.productadda.service.order.InvoiceDownloadService;
import com.productadda.service.order.InvoiceGenerationService;

import com.productadda.service.order.InvoiceQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InvoiceController {

        private final InvoiceGenerationService invoiceGenerationService;

        private final InvoiceQueryService invoiceQueryService;
        private final InvoiceDownloadService invoiceDownloadService;

        // ==========================================
        // GET INVOICES (PAGINATED)
        // Description: Allows looking up tracked invoice mapping elements for the
        // authenticated session user without exposing or requiring direct resource
        // keys.
        // Clean Layering: Offloaded cleanly to InvoiceQueryService.
        // ==========================================
        @GetMapping("/api/orders/invoices")
        public ResponseEntity<ApiSuccessResponseDto<UserInvoiceWrapperDto>> getInvoices(
                        @PageableDefault(size = 10, page = 0) Pageable pageable) {

                // Spring context handles query parameters (?page=0&size=10) and safely
                // translates them to Pageable instances
                UserInvoiceWrapperDto response = invoiceQueryService.getUserInvoices(pageable);

                return ResponseEntity.status(HttpStatus.OK).body(ApiSuccessResponseDto.<UserInvoiceWrapperDto>builder()
                                .success(true)
                                .message("Invoice track metadata loaded successfully.")
                                .data(response)
                                .build());
        }

        // ==========================================
        // GET INVOICE BY ORDER ID
        // Description: Allows looking up tracked invoice mapping elements
        // directly via the target order key context.
        // Clean Layering: Offloaded to InvoiceQueryService.
        // ==========================================
        @GetMapping("/api/orders/{orderId}/invoices")
        public ResponseEntity<ApiSuccessResponseDto<InvoiceResponseDto>> getInvoiceByOrderId(
                        @PathVariable UUID orderId) {

                InvoiceResponseDto response = invoiceQueryService.getInvoiceByOrderId(orderId);

                return ResponseEntity.status(HttpStatus.OK).body(ApiSuccessResponseDto.<InvoiceResponseDto>builder()
                                .success(true)
                                .message("Invoice track metadata loaded successfully.")
                                .data(response)
                                .build());
        }

        // ==========================================
        // UNIFIED INVOICE GENERATE ENDPOINT
        // Description: Triggers initial compilation or checks modification
        // hashes to perform conditional updates.
        // Clean Layering: Handled cleanly via InvoiceGenerationService.
        // ==========================================
        @PostMapping("/api/orders/{orderId}/invoices/generate")
        public ResponseEntity<ApiSuccessResponseDto<InvoiceResponseDto>> generateInvoice(
                        @PathVariable UUID orderId) {

                InvoiceResponseDto response = invoiceGenerationService.generateInvoice(orderId);

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
        // INVOICE DOWNLOAD LINK GENERATOR (JSON RESPONSE)
        // Description: Securely fetches private keys and provides a
        // copy-pasteable short-lived URL configuration within a DTO wrapper.
        // Clean Layering: Offloaded to InvoiceQueryService.
        // ==========================================
        @GetMapping("/api/orders/invoices/{invoiceId}/download")
        public ResponseEntity<ApiSuccessResponseDto<InvoiceDownloadResponseDto>> downloadInvoice(
                        @PathVariable UUID invoiceId) {

                InvoiceDownloadResponseDto data = invoiceDownloadService.invoiceDownload(invoiceId);

                return ResponseEntity.status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<InvoiceDownloadResponseDto>builder()
                                                .success(true)
                                                .message("Presigned URL secured successfully. Valid for 15 minutes.")
                                                .data(data)
                                                .build());
        }
}