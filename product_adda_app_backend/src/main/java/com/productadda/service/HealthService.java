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

    @Transactional(readOnly = true)
    public HealthResponseDto getDatabaseHealth() {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: Measures connectivity metrics and captures system clock
         * times across native database diagnostic execution gates.
         * ================================================================
         */

        // ==========================================
        // 1.1 LATENCY TIMER INITIALIZATION
        // ==========================================
        long startTime = System.currentTimeMillis();

        try {
            // ==========================================
            // 1.2 NATIVE CONNECTION VALIDATION
            // ==========================================
            Object result = entityManager
                    .createNativeQuery("SELECT 1")
                    .getSingleResult();

            long endTime = System.currentTimeMillis();

            /*
             * ================================================================
             * 5. RESPONSE SECTION
             * Description: Builds structural connection payloads inline.
             * ================================================================
             */

            // ==========================================
            // 5.1 RESPONSE MAPPING
            // ==========================================
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

    public Map<String, Object> getApplicationHealth() {

        /*
         * ================================================================
         * 2. BUSINESS SECTION
         * Description: Resolves low-level JVM operating tracking states and
         * aggregates multi-tier execution memory footprints.
         * ================================================================
         */

        // ==========================================
        // 2.1 RUNTIME COMPILATION METRICS
        // ==========================================
        long uptime = java.lang.management.ManagementFactory
                .getRuntimeMXBean()
                .getUptime();

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long absoluteFreeMemory = freeMemory + (maxMemory - allocatedMemory);

        /*
         * ================================================================
         * 5. RESPONSE SECTION
         * Description: Packages diagnostic telemetry trees cleanly into runtime Maps.
         * ================================================================
         */

        // ==========================================
        // 5.1 SUB-METRIC SCHEMAS COLLECTION
        // ==========================================
        Map<String, Object> memoryMetrics = new HashMap<>();
        memoryMetrics.put("maxMemoryMb", maxMemory / (1024 * 1024));
        memoryMetrics.put("allocatedMemoryMb", allocatedMemory / (1024 * 1024));
        memoryMetrics.put("freeMemoryMb", absoluteFreeMemory / (1024 * 1024));

        // ==========================================
        // 5.2 CONTAINER WRAPPING AND RESPONSE
        // ==========================================
        Map<String, Object> healthReport = new HashMap<>();
        healthReport.put("status", "UP");
        healthReport.put("version", "1.0.0");
        healthReport.put("uptimeMs", uptime);
        healthReport.put("timestampUtc", LocalDateTime.now(ZoneOffset.UTC).toString());
        healthReport.put("systemMemoryMetrics", memoryMetrics);

        return healthReport;
    }
}