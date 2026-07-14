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
public class VendorPayoutScheduleRequestDto {

    // Optional: schedule payout for a specific vendor only. Null/omitted
    // means "iterate every active vendor."
    private String filterVendorId;
}
