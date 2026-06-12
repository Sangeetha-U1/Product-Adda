package com.productadda.dto;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Data
@Builder
@JsonPropertyOrder({ "status", "message", "data" })
public class ApiResponse {

    private boolean status;
    private String message;
    private Object data;
}