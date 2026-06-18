package com.productadda.service;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.HealthResponseDto;
import com.productadda.exception.ApiException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HealthService {

    @PersistenceContext
    private final EntityManager entityManager;

    /*
     * ================================================================
     * GET DATABASE HEALTH
     * Description: Executes a database-agnostic ping check to compute connection
     * latency.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public HealthResponseDto getDatabaseHealth() {
        long startTime = System.currentTimeMillis();

        try {
            // FIX: Swapped out database-specific syntax for an ultra-fast, universally
            // legal ping check
            Object result = entityManager
                    .createNativeQuery("SELECT 1")
                    .getSingleResult();

            long endTime = System.currentTimeMillis();

            return HealthResponseDto.builder()
                    .status("UP")
                    .database("CONNECTED")
                    .dbVersion(result != null ? "ACTIVE" : "UNKNOWN")
                    .latencyMs(endTime - startTime)
                    .build();

        } catch (Exception exception) {
            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Database connection validation probe failed securely");
        }
    }

    /*
     * ================================================================
     * GET APPLICATION HEALTH
     * Description: Gathers explicit runtime JVM diagnostic system stats inside a
     * typed Map.
     * ================================================================
     */
    public Map<String, Object> getApplicationHealth() {
        long uptime = java.lang.management.ManagementFactory
                .getRuntimeMXBean()
                .getUptime();

        // Advanced Monitoring metrics tracking initialization
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long absoluteFreeMemory = freeMemory + (maxMemory - allocatedMemory);

        Map<String, Object> healthReport = new HashMap<>();

        healthReport.put("status", "UP");
        healthReport.put("version", "1.0.0");
        healthReport.put("uptimeMs", uptime);
        healthReport.put("timestampUtc", LocalDateTime.now(ZoneOffset.UTC).toString());

        // Memory diagnostic payloads added to boost production DX dashboard
        // observability
        Map<String, Object> memoryMetrics = new HashMap<>();
        memoryMetrics.put("maxMemoryMb", maxMemory / (1024 * 1024));
        memoryMetrics.put("allocatedMemoryMb", allocatedMemory / (1024 * 1024));
        memoryMetrics.put("freeMemoryMb", absoluteFreeMemory / (1024 * 1024));

        healthReport.put("systemMemoryMetrics", memoryMetrics);

        return healthReport;
    }
}