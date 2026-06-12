package com.productadda.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.HealthResponseDto;
import com.productadda.dto.RepositoryHealthResponseDto;
import com.productadda.service.HealthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

        private final HealthService healthService;

        @GetMapping("/db")
        public ResponseEntity<ApiSuccessResponseDto<HealthResponseDto>> dbHealth() {

                HealthResponseDto dto = healthService.getDatabaseHealth();

                return ResponseEntity.ok(
                                ApiSuccessResponseDto.<HealthResponseDto>builder()
                                                .success(true)
                                                .message("Database connection successful")
                                                .data(dto)
                                                .build());
        }

        @GetMapping("/app")
        public ResponseEntity<ApiSuccessResponseDto<Object>> appHealth() {

                return ResponseEntity.ok(
                                ApiSuccessResponseDto.builder()
                                                .success(true)
                                                .message("Application is running")
                                                .data(healthService.getApplicationHealth())
                                                .build());
        }

        // TODO: Only for testing, delete later.
        @GetMapping("/repository")
        public ResponseEntity<ApiSuccessResponseDto<RepositoryHealthResponseDto>> repositoryHealth() {

                RepositoryHealthResponseDto dto = healthService.getRepositoryHealth();

                return ResponseEntity.ok(
                                ApiSuccessResponseDto.<RepositoryHealthResponseDto>builder()
                                                .success(true)
                                                .message("Repository verification successful")
                                                .data(dto)
                                                .build());
        }
}