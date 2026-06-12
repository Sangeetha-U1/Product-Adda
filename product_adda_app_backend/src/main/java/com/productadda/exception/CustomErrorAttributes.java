package com.productadda.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Component
public class CustomErrorAttributes
                extends DefaultErrorAttributes {

        @Override
        public Map<String, Object> getErrorAttributes(
                        WebRequest webRequest,

                        ErrorAttributeOptions options) {

                Throwable error = getError(webRequest);

                int status = 500;

                String message = "Internal server error";

                Map<String, Object> defaultAttributes = super.getErrorAttributes(
                                webRequest,
                                options);

                Object defaultMessage = defaultAttributes.get(
                                "message");

                if (defaultMessage != null
                                && !defaultMessage.toString().isBlank()) {

                        message = defaultMessage.toString();
                }

                if (error instanceof org.springframework.web.ErrorResponseException ex) {

                        status = ex.getStatusCode().value();

                        message = ex.getBody().getDetail();
                }

                Map<String, Object> errorBody = new LinkedHashMap<>();

                errorBody.put(
                                "code",
                                status);

                errorBody.put(
                                "type",
                                org.springframework.http.HttpStatus
                                                .valueOf(status)
                                                .name());

                Map<String, Object> response = new LinkedHashMap<>();

                response.put(
                                "success",
                                false);

                response.put(
                                "message",
                                message);

                response.put(
                                "error",
                                errorBody);

                return response;
        }
}
