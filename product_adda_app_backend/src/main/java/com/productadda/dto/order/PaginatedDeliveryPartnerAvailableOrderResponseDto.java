package com.productadda.dto.order;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedDeliveryPartnerAvailableOrderResponseDto {

    private List<DeliveryPartnerAvailableOrderListDto> orders;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Integer pageSize;
    private Boolean isFirst;
    private Boolean isLast;
}