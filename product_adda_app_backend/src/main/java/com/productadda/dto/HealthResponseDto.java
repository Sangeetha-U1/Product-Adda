package com.productadda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class HealthResponseDto {

    private String status;
    private String database;
    private String dbVersion;
    private Long latencyMs;
}