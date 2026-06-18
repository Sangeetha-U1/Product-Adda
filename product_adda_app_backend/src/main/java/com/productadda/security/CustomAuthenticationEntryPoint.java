package com.productadda.security;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.productadda.dto.ApiErrorResponseDto;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public void commence(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException)
                        throws IOException, ServletException {

                // 1. Check if our Filter passed down a specific JWT exception type/message
                String errorType = (String) request.getAttribute("jwt_error_type");
                String errorMessage = (String) request.getAttribute("jwt_error_message");

                // 2. Fallback to generic defaults if it wasn't a JWT issue (e.g., missing
                // header altogether)
                if (errorType == null) {
                        errorType = HttpStatus.UNAUTHORIZED.name();
                }
                if (errorMessage == null) {
                        errorMessage = "Authentication required";
                }

                // 3. Build your existing structured DTO format
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("code", HttpStatus.UNAUTHORIZED.value());
                error.put("type", errorType);

                ApiErrorResponseDto responseDto = new ApiErrorResponseDto(
                                false,
                                errorMessage,
                                error);

                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");

                objectMapper.writeValue(
                                response.getOutputStream(),
                                responseDto);
        }
}