package com.productadda.dto.payment;

import jakarta.validation.constraints.NotBlank;

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
public class PaymentMethodRequestDto {

    // razorpay only, per project scope
    @NotBlank(message = "Payment gateway is required")
    private String paymentGateway;

    // card / upi / netbanking / wallet
    @NotBlank(message = "Method type is required")
    private String methodType;

    @NotBlank(message = "Gateway token id is required")
    private String gatewayTokenId;

    // Required only when methodType = "card"
    private String cardLastFour;

    private String cardBrand;

    private Integer cardExpiryMonth;

    private Integer cardExpiryYear;

    private Boolean isPrimary;
}
