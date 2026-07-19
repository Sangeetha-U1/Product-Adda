package com.productadda.dto.report;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogReportResponseDto {

    private List<AuditLogReportItemDto> items;

    // Opaque token to pass as the next request's cursor. Null when no
    // further page exists.
    private String nextCursor;

    private Boolean hasMore;
}
