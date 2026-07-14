package com.productadda.dto.payment;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedAuditLogResponseDto {

    private List<AuditLogResponseDto> content;

    private long totalElements;

    private int page;

    private int size;
}
