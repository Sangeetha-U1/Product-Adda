package com.productadda.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonValue;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCreateResponseDto {

    private java.util.Map<String, Object> rawLinkDetails;

    @JsonValue
    public java.util.Map<String, Object> getRawLinkDetails() {
        return this.rawLinkDetails;
    }
}
