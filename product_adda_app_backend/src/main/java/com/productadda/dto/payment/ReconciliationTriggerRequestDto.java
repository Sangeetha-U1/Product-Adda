package com.productadda.dto.payment;

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
public class ReconciliationTriggerRequestDto {

    // Optional: ISO-8601 UTC datetime. Defaults to NOW - 24h when omitted.
    private String pastDate;

    // Optional: ISO-8601 UTC datetime. Defaults to NOW when omitted.
    private String tillDate;
}
