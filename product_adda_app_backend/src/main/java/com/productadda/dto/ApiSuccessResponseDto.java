package com.productadda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApiSuccessResponseDto<T> {

    private Boolean success;
    private String message;
    private T data;
}