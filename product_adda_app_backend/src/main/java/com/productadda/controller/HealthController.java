package com.productadda.controller;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.HealthResponseDto;
import com.productadda.exception.ApiException; // Import your custom exception

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health")
public class HealthController {

        @PersistenceContext
        private EntityManager entityManager;

        @GetMapping("/db")
        public ResponseEntity<ApiSuccessResponseDto<HealthResponseDto>> dbHealth() {

                long startTime = System.currentTimeMillis();

                try {
                        Object result = entityManager
                                        .createNativeQuery("SELECT VERSION()")
                                        .getSingleResult();

                        long endTime = System.currentTimeMillis();

                        HealthResponseDto dto = HealthResponseDto.builder()
                                        .status("UP")
                                        .database("CONNECTED")
                                        .dbVersion(result != null ? result.toString() : "UNKNOWN")
                                        .latencyMs(endTime - startTime)
                                        .build();

                        return ResponseEntity.ok(
                                        ApiSuccessResponseDto.<HealthResponseDto>builder()
                                                        .success(true)
                                                        .message("Database connection successful")
                                                        .data(dto)
                                                        .build());

                } catch (Exception e) {
                        throw new ApiException(
                                        HttpStatus.SERVICE_UNAVAILABLE,
                                        "Database connection failed");
                }
        }

        @GetMapping("/app")
        public ResponseEntity<ApiSuccessResponseDto<Object>> appHealth() {

                long uptime = java.lang.management.ManagementFactory
                                .getRuntimeMXBean()
                                .getUptime();

                String version = "1.0.0"; // can later move to application.properties

                Map<String, Object> data = new HashMap<>();
                data.put("status", "UP");
                data.put("uptimeMs", uptime);
                data.put("version", version);

                return ResponseEntity.ok(
                                ApiSuccessResponseDto.builder()
                                                .success(true)
                                                .message("Application is running")
                                                .data(data)
                                                .build());
        }
}