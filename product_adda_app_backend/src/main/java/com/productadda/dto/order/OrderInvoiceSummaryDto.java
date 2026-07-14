package com.productadda.dto.order;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderInvoiceSummaryDto {

    private UUID invoiceId;

    private String invoiceNumber;
}