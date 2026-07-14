package com.productadda.dto.deliverypartner;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * RESPONSE DTO
 * Description: Paginated wrapper shared by the delivery partner
 * "my orders" (Phase 2) and "delivery history" (Phase 5) endpoints.
 * ================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedDeliveryPartnerMyOrderResponseDto {

    private List<DeliveryPartnerMyOrderListDto> orders;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Integer pageSize;
    private Boolean isFirst;
    private Boolean isLast;
}
