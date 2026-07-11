package com.productadda.dto.order;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * ================================================================
 * REQUEST DTO
 * Description: Payload for admin-initiated delivery partner
 * assignment to a SHIPPED order.
 * ================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAssignmentRequestDto {

    @NotNull(message = "deliveryPartnerId is required")
    private UUID deliveryPartnerId;
}
