package com.productadda.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ApiException extends RuntimeException {

    // Lombok's @NonNull ensures this can NEVER be instantiated as null
    private final HttpStatus status;

    public ApiException(@NonNull HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}