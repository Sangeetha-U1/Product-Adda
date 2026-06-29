package com.productadda.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.productadda.dto.ApiErrorResponseDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

        /**
         * Dynamic internal helper to construct uniform error map payloads
         * directly from Spring HttpStatusCode/HttpStatus sources.
         */
        private Map<String, Object> createDynamicErrorDetails(HttpStatusCode statusCode) {
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("code", statusCode.value());
                if (statusCode instanceof HttpStatus status) {
                        error.put("type", status.name());
                } else {
                        error.put("type", statusCode.toString());
                }
                return error;
        }

        @ExceptionHandler(ApiException.class)
        public ResponseEntity<ApiErrorResponseDto> handleApiException(ApiException exception) {
                HttpStatus status = exception.getStatus();
                Map<String, Object> error = createDynamicErrorDetails(status);

                ApiErrorResponseDto response = new ApiErrorResponseDto(
                                false,
                                exception.getMessage(),
                                error);

                return ResponseEntity.status(status).body(response);
        }

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<ApiErrorResponseDto> handleNoResourceFoundException(NoResourceFoundException exception) {
                HttpStatusCode statusCode = exception.getStatusCode();
                Map<String, Object> error = createDynamicErrorDetails(statusCode);

                ApiErrorResponseDto response = new ApiErrorResponseDto(
                                false,
                                "Route not found: " + exception.getResourcePath(),
                                error);

                return ResponseEntity.status(statusCode).body(response);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponseDto> handleMethodArgumentNotValidException(
                        MethodArgumentNotValidException exception) {
                HttpStatusCode statusCode = exception.getStatusCode();
                Map<String, Object> error = createDynamicErrorDetails(statusCode);

                String message = exception.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .findFirst()
                                .map(fieldError -> fieldError.getDefaultMessage())
                                .orElse("Validation failed");

                ApiErrorResponseDto response = new ApiErrorResponseDto(
                                false,
                                message,
                                error);

                return ResponseEntity.status(statusCode).body(response);
        }

        @ExceptionHandler(ErrorResponseException.class)
        public ResponseEntity<ApiErrorResponseDto> handleErrorResponseException(ErrorResponseException exception) {
                HttpStatusCode statusCode = exception.getStatusCode();
                Map<String, Object> error = createDynamicErrorDetails(statusCode);

                String detailMessage = "An error occurred";
                var body = exception.getBody();
                if (body != null && body.getDetail() != null) {
                        detailMessage = body.getDetail();
                }

                ApiErrorResponseDto response = new ApiErrorResponseDto(
                                false,
                                detailMessage,
                                error);

                return ResponseEntity.status(statusCode).body(response);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiErrorResponseDto> handleAccessDeniedException(AccessDeniedException exception) {
                HttpStatusCode statusCode = HttpStatus.FORBIDDEN;
                String detailMessage = exception.getMessage();

                if (exception instanceof ErrorResponse errorResponse) {
                        statusCode = errorResponse.getStatusCode();
                        if (errorResponse.getBody().getDetail() != null) {
                                detailMessage = errorResponse.getBody().getDetail();
                        }
                }

                Map<String, Object> error = createDynamicErrorDetails(statusCode);
                ApiErrorResponseDto response = new ApiErrorResponseDto(
                                false,
                                detailMessage,
                                error);

                return ResponseEntity.status(statusCode).body(response);
        }

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ApiErrorResponseDto> handleMaxSizeException(MaxUploadSizeExceededException exception) {
                HttpStatusCode statusCode = HttpStatus.BAD_REQUEST;
                String detailMessage = "Upload failed: File size exceeds the allowed limit (Max: 5MB per file / 25MB total payload)";

                if (exception instanceof ErrorResponse errorResponse) {
                        statusCode = errorResponse.getStatusCode();
                        if (errorResponse.getBody().getDetail() != null) {
                                detailMessage = errorResponse.getBody().getDetail();
                        }
                }

                Map<String, Object> error = createDynamicErrorDetails(statusCode);
                ApiErrorResponseDto response = new ApiErrorResponseDto(
                                false,
                                detailMessage,
                                error);

                return ResponseEntity.status(statusCode).body(response);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiErrorResponseDto> handleMethodArgumentTypeMismatchException(
                        MethodArgumentTypeMismatchException exception) {
                // Spring sets bad request status directly inside ErrorResponse capabilities
                // since v6
                HttpStatusCode statusCode = HttpStatus.BAD_REQUEST;

                String parameterName = exception.getName();
                String invalidValue = exception.getValue() != null ? exception.getValue().toString() : "null";
                String requiredType = exception.getRequiredType() != null ? exception.getRequiredType().getSimpleName()
                                : "required type";

                String cleanMessage = String.format(
                                "Invalid value '%s' provided for parameter '%s'. Expected format: %s.",
                                invalidValue, parameterName, requiredType);

                Map<String, Object> error = createDynamicErrorDetails(statusCode);
                ApiErrorResponseDto response = new ApiErrorResponseDto(
                                false,
                                cleanMessage,
                                error);

                return ResponseEntity.status(statusCode).body(response);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiErrorResponseDto> handleHttpMessageNotReadableException(
                        HttpMessageNotReadableException exception) {
                HttpStatusCode statusCode = HttpStatus.BAD_REQUEST;
                String cleanMessage = "Required request body is missing or malformed. Please provide a valid JSON payload.";

                if (exception.getMessage() != null
                                && exception.getMessage().contains("Required request body is missing")) {
                        cleanMessage = "Required request body is entirely missing. Please provide the required JSON payload properties.";
                }

                Map<String, Object> error = createDynamicErrorDetails(statusCode);
                ApiErrorResponseDto response = new ApiErrorResponseDto(
                                false,
                                cleanMessage,
                                error);

                return ResponseEntity.status(statusCode).body(response);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponseDto> handleGenericException(Exception exception) {
                HttpStatusCode statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
                String message = "Internal server error";

                if (exception instanceof ErrorResponse ex) {
                        statusCode = ex.getStatusCode();
                        var body = ex.getBody();
                        if (body != null && body.getDetail() != null && !body.getDetail().isBlank()) {
                                message = body.getDetail();
                        }
                } else if (exception instanceof ApiException ex) {
                        statusCode = ex.getStatus();
                        message = ex.getMessage();
                } else if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
                        message = exception.getMessage();
                }

                Map<String, Object> error = createDynamicErrorDetails(statusCode);
                ApiErrorResponseDto response = new ApiErrorResponseDto(
                                false,
                                message,
                                error);

                return ResponseEntity.status(statusCode).body(response);
        }
}