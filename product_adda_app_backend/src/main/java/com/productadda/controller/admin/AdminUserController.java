package com.productadda.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.admin.AdminUserCreateRequestDto;
import com.productadda.dto.admin.AdminUserCreateResponseDto;
import com.productadda.service.admin.AdminUserCreateService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserCreateService adminUserCreateService;

    @PostMapping("/users")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiSuccessResponseDto<AdminUserCreateResponseDto>> createAdminUser(
            @Valid @RequestBody AdminUserCreateRequestDto request) {

        AdminUserCreateResponseDto responseData = adminUserCreateService.createAdminUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiSuccessResponseDto.<AdminUserCreateResponseDto>builder()
                        .success(true)
                        .message("Administrative account structure provisioned and activated successfully")
                        .data(responseData)
                        .build());
    }
}