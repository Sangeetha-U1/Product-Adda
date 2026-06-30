package com.productadda.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.context.request.WebRequest;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {

        @Override
        public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {

                Throwable error = getError(webRequest); // This still works in SB 4
                Map<String, Object> defaultAttributes = super.getErrorAttributes(webRequest, options);

                int status = 500;
                String message = "Internal server error";

                // Extract message from default attributes
                Object defaultMessage = defaultAttributes.get("message");
                if (defaultMessage != null && !defaultMessage.toString().isBlank()) {
                        message = defaultMessage.toString();
                }

                // Handle ErrorResponse (newer way in Spring Boot 4 / Spring 6+)
                if (error instanceof ErrorResponse errorResponse) {
                        status = errorResponse.getStatusCode().value();
                        if (errorResponse.getBody() != null && errorResponse.getBody().getDetail() != null) {
                                message = errorResponse.getBody().getDetail();
                        }
                }

                // Build your custom response
                Map<String, Object> errorBody = new LinkedHashMap<>();
                errorBody.put("code", status);
                errorBody.put("type", HttpStatus.valueOf(status).name());

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", false);
                response.put("message", message);
                response.put("error", errorBody);

                return response;
        }
}