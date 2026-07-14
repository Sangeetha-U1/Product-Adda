package com.productadda.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueTrendPointDto {

    // ISO date string, e.g. "2026-06-20"
    private String date;

    private Long revenueInPaise;
}
