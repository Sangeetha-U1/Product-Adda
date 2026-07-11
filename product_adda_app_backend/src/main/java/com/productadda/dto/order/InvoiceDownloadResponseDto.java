package com.productadda.dto.order;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDownloadResponseDto {

    private String invoiceNumber;

    private Integer invoiceVersion;

    private String presignedUrl;

    private LocalDateTime urlExpirationUtc;
}