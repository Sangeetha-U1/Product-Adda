package com.productadda.controller.report;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.report.SystemHealthResponseDto;
import com.productadda.service.report.SystemHealthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SystemHealthController {

    private final SystemHealthService systemHealthService;

    // ==========================================
    // GET REPORT MODULE SYSTEM HEALTH
    // Description: Live health check across components that actually exist
    // (database, snapshot tables). Components with no real infrastructure
    // yet (cache, S3, background worker) are reported honestly as
    // NOT_CONFIGURED / NOT_IMPLEMENTED rather than simulated. Any
    // authenticated user may call this endpoint.
    // Clean Layering: Offloaded to SystemHealthService.
    // ==========================================
    @GetMapping("/api/reports/system-health")
    public ResponseEntity<ApiSuccessResponseDto<SystemHealthResponseDto>> getSystemHealth() {

        SystemHealthResponseDto response = systemHealthService.getSystemHealth();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponseDto.<SystemHealthResponseDto>builder()
                        .success(true)
                        .message("System health retrieved")
                        .data(response)
                        .build());
    }
}
