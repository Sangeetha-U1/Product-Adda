package com.productadda.dto.payment;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorPayoutResponseDto {

    private String id;

    private String vendorId;

    private String vendorBusinessName;

    private LocalDate periodStartDate;

    private LocalDate periodEndDate;

    private Long totalAmountInPaise;

    private Long platformCommissionInPaise;

    private Long netPayoutAmountInPaise;

    private String status;

    private LocalDate scheduledPayoutDate;

    private LocalDateTime createdAtUtc;
}
