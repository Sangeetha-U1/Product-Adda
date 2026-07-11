package com.productadda.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * RESPONSE DTO
 * Description: Sanitized, frontend-ready confirmation payload
 * returned after invoice generation or regeneration.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponseDto {

    private UUID invoiceId;

    private String invoiceNumber;

    private Integer invoiceVersion;

    private UUID orderId;

    private BigDecimal invoiceAmount;

    private LocalDateTime generatedAt;

    private String fileUrl;

    // private String presignedUrl;
    
    // private LocalDateTime urlExpirationUtc;
}
