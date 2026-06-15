package com.productadda.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.productadda.dto.HealthResponseDto;
import com.productadda.exception.ApiException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HealthService {

    @PersistenceContext
    private EntityManager entityManager;

    public HealthResponseDto getDatabaseHealth() {

        long startTime = System.currentTimeMillis();

        try {

            Object result = entityManager
                    .createNativeQuery("SELECT VERSION()")
                    .getSingleResult();

            long endTime = System.currentTimeMillis();

            return HealthResponseDto.builder()
                    .status("UP")
                    .database("CONNECTED")
                    .dbVersion(result != null ? result.toString() : "UNKNOWN")
                    .latencyMs(endTime - startTime)
                    .build();

        } catch (Exception exception) {

            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Database connection failed");
        }
    }

    public Map<String, Object> getApplicationHealth() {

        long uptime = java.lang.management.ManagementFactory
                .getRuntimeMXBean()
                .getUptime();

        String version = "1.0.0";

        Map<String, Object> data = new HashMap<>();

        data.put("status", "UP");
        data.put("uptimeMs", uptime);
        data.put("version", version);

        return data;
    }
}