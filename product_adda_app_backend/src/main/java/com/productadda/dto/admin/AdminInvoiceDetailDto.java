package com.productadda.dto.admin;

import java.math.BigDecimal;
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
public class AdminInvoiceDetailDto {

    // Complete structural metadata mapping representing an unmasked invoice core
    // record
    private UUID invoiceId;

    private String invoiceNumber;

    private Integer invoiceVersion;

    private UUID orderId;

    private String orderNumber;

    private UUID customerId;

    private String customerEmail;

    private BigDecimal invoiceAmount;

    private String fileUrl;

    private String objectKey;

    private LocalDateTime generatedAt;

    private Boolean isActive;

    private LocalDateTime createdAtUtc;

    private LocalDateTime updatedAtUtc;
}