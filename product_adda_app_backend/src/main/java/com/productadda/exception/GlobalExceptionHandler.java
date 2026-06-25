package com.productadda.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;

import com.productadda.dto.ApiErrorResponseDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ApiException.class)
        public ResponseEntity<ApiErrorResponseDto> handleApiException(
                        ApiException exception) {

                Map<String, Object> error = new LinkedHashMap<>();

                // Safe to use directly now because Lombok's @NonNull guarantees it's not null
                HttpStatus status = exception.getStatus();

                error.put("code", status.value());
                error.put("type", status.name());

                // Using your Lombok-powered DTO constructor
                ApiErrorResponseDto response = new ApiErrorResponseDto(
                                false,
                                exception.getMessage(),
                                error);

                return ResponseEntity
                                .status(status)
                                .body(response);
        }

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<ApiErrorResponseDto> handleNoResourceFoundException(
                        NoResourceFoundException exception) {

                Map<String, Object> error = new LinkedHashMap<>();
                error.put("code", 404);
                error.put("type", "NOT_FOUND");

                ApiErrorResponseDto response = new ApiErrorResponseDto(
                                false,
                                "Route not found",
                                error);

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(response);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponseDto> handleMethodArgumentNotValidException(
                        MethodArgumentNotValidException exception) {

                String message = exception.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .findFirst()
                                .map(fieldError -> fieldError.getDefaultMessage())
                                .orElse("Validation failed");

                Map<String, Object> error = new LinkedHashMap<>();
                error.put("code", 400);
                error.put("type", "BAD_REQUEST");

                ApiErrorResponseDto response = new ApiErrorResponseDto(
                                false,
                                message,
                                error);

                return ResponseEntity
                                .badRequest()
                                .body(response);
        }

        @ExceptionHandler(ErrorResponseException.class)
        public ResponseEntity<ApiErrorResponseDto> handleErrorResponseException(
                        ErrorResponseException exception) {

                Map<String, Object> error = new LinkedHashMap<>();
                error.put("code", exception.getStatusCode().value());
                error.put("type", exception.getStatusCode().toString());

                // Fixed: Local variable assignment protects against potential null pointer
                // warnings
                String detailMessage = "An error occurred";
                var body = exception.getBody();
                if (body != null && body.getDetail() != null) {
                        detailMessage = body.getDetail();
                }

                ApiErrorResponseDto response = new ApiErrorResponseDto(
                                false,
                                detailMessage,
                                error);

                return ResponseEntity
                                .status(exception.getStatusCode())
                                .body(response);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponseDto> handleGenericException(
                        Exception exception) {

                int statusCode = 500;
                String statusType = "INTERNAL_SERVER_ERROR";
                String message = "Internal server error";

                if (exception instanceof ErrorResponseException ex) {
                        statusCode = ex.getStatusCode().value();
                        statusType = ex.getStatusCode().toString();

                        // Fixed: Keeps the compiler happy for Spring's internal methods
                        var body = ex.getBody();
                        if (body != null) {
                                String detail = body.getDetail();
                                if (detail != null && !detail.isBlank()) {
                                        message = detail;
                                }
                        }
                }

                else if (exception instanceof ApiException ex) {
                        statusCode = ex.getStatus().value();
                        statusType = ex.getStatus().name();
                        message = ex.getMessage();
                }

                else if (exception.getMessage() != null
                                && !exception.getMessage().isBlank()) {
                        message = exception.getMessage();
                }

                Map<String, Object> error = new LinkedHashMap<>();
                error.put("code", statusCode);
                error.put("type", statusType);

                ApiErrorResponseDto response = new ApiErrorResponseDto(
                                false,
                                message,
                                error);

                return ResponseEntity
                                .status(statusCode)
                                .body(response);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiErrorResponseDto> handleAccessDeniedException(AccessDeniedException exception) {

                Map<String, Object> error = new LinkedHashMap<>();

                // Default fallbacks in case it's a raw security exception
                int statusCode = HttpStatus.FORBIDDEN.value();
                String statusType = HttpStatus.FORBIDDEN.name();
                String detailMessage = exception.getMessage();

                // If it implements Spring's ErrorResponse interface, extract everything
                // dynamically
                if (exception instanceof ErrorResponse errorResponse) {
                        statusCode = errorResponse.getStatusCode().value();
                        statusType = errorResponse.getStatusCode().toString();
                        if (errorResponse.getBody().getDetail() != null) {
                                detailMessage = errorResponse.getBody().getDetail();
                        }
                }

                error.put("code", statusCode);
                error.put("type", statusType);

                ApiErrorResponseDto response = new ApiErrorResponseDto(
                                false,
                                detailMessage,
                                error);

                return ResponseEntity
                                .status(statusCode)
                                .body(response);
        }
}