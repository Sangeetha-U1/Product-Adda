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
public class PaymentVerifyResponseDto {

    private Boolean verified;

    private String paymentStatus;

    private String orderStatus;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String message;

    private String orderId;
    
}
