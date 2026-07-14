package com.productadda.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationDiscrepancyDto {

    private String gatewayTransactionId;

    private Long amountInPaise;

    // Only populated for orphaned_in_db entries (DB has it, gateway didn't return it)
    private String paymentId;
}
