package com.productadda.dto.order;

import java.util.List;

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
public class PaginatedAdminInvoiceResponseDto {

    // Houses the targeted dataset slice along with exact pagination state variables
    private List<AdminInvoiceDetailDto> content;

    private long totalElements;

    private int page;

    private int size;
}