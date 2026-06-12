package com.productadda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApiErrorResponseDto {

    private Boolean success;
    private String message;
    private Object error;
}