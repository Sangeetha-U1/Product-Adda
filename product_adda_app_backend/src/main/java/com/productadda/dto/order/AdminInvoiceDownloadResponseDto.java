package com.productadda.dto.order;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminInvoiceDownloadResponseDto {

    private UUID invoiceId;

    private String invoiceNumber;

    private String orderNumber;

    // Short-lived transient asset signature URL string
    private String presignedUrl;

    private LocalDateTime urlExpirationUtc;
}