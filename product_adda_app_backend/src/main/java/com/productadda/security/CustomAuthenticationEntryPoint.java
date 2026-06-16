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
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {

        Map<String, Object> error = new LinkedHashMap<>();

        error.put("code", HttpStatus.UNAUTHORIZED.value());
        error.put("type", HttpStatus.UNAUTHORIZED.name());

        ApiErrorResponseDto responseDto = new ApiErrorResponseDto(
                false,
                "Authentication required",
                error);

        response.setStatus(
                HttpStatus.UNAUTHORIZED.value());

        response.setContentType(
                "application/json");

        objectMapper.writeValue(
                response.getOutputStream(),
                responseDto);
    }
}