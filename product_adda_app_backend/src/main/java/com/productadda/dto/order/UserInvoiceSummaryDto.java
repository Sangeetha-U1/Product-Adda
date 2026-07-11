package com.productadda.dto.order;

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
public class UserInvoiceSummaryDto {

    // Places metadata references cleanly inside a dedicated user summary payload
    private UUID invoiceId;

    private String invoiceNumber;

    private Integer invoiceVersion;

    private UUID orderId;

    private String orderNumber;

    private BigDecimal invoiceAmount;

    private LocalDateTime generatedAt;

    private String fileUrl;

    private String presignedUrl;
}