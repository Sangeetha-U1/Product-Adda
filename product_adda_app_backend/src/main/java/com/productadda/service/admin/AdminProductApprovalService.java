package com.productadda.service.admin;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.product.AdminProductApprovalResponseDto;
import com.productadda.dto.product.AdminProductRejectionRequestDto;
import com.productadda.entity.Product;
import com.productadda.entity.ProductStatus;
import com.productadda.exception.ApiException;
import com.productadda.repository.ProductRepository;
import com.productadda.repository.ProductStatusRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminProductApprovalService {

    private final ProductRepository productRepository;
    private final ProductStatusRepository productStatusRepository;

    @Transactional
    public AdminProductApprovalResponseDto approveProduct(UUID productId) {
        if (productId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product unique operational indicator must not be null");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Security context is missing raw authentication data");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SUPER_ADMIN") || a.getAuthority().equals("ADMIN"));
        if (!isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied: Only admins can access moderation endpoints");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found with ID: " + productId));

        String currentStatus = product.getFkStatus() != null ? product.getFkStatus().getStatusCode() : "UNKNOWN";
        if ("APPROVED".equalsIgnoreCase(currentStatus)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Product is already in APPROVED status and cannot be approved again");
        }
        if (!"PENDING_APPROVAL".equalsIgnoreCase(currentStatus) && !"DRAFT".equalsIgnoreCase(currentStatus)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product is already in " + currentStatus + " status");
        }

        ProductStatus approvedStatus = productStatusRepository.findByStatusCode("APPROVED")
                .orElseThrow(
                        () -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Configured mapping target missing"));

        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        product.setFkStatus(approvedStatus);
        product.setUpdatedAtUtc(nowUtc);

        Product savedProduct = productRepository.save(product);
        String adminEmail = authentication.getName() != null ? authentication.getName() : "admin@productadda.com";

        return AdminProductApprovalResponseDto.builder()
                .productId(savedProduct.getPkProductId())
                .productName(savedProduct.getTitle())
                .status("APPROVED")
                .approvedAtUtc(nowUtc.toString())
                .approvedByAdmin(adminEmail)
                .build();
    }

    @Transactional
    public AdminProductApprovalResponseDto rejectProduct(UUID productId, AdminProductRejectionRequestDto request) {
        if (productId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product unique operational indicator must not be null");
        }
        if (request == null || request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Rejection reason is required and cannot be blank");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Security context is missing raw authentication data");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SUPER_ADMIN") || a.getAuthority().equals("ADMIN"));
        if (!isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied: Only admins can access moderation endpoints");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found with ID: " + productId));

        String currentStatus = product.getFkStatus() != null ? product.getFkStatus().getStatusCode() : "UNKNOWN";
        if (!"PENDING_APPROVAL".equalsIgnoreCase(currentStatus) && !"DRAFT".equalsIgnoreCase(currentStatus)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product is already in " + currentStatus + " status");
        }

        ProductStatus rejectedStatus = productStatusRepository.findByStatusCode("REJECTED")
                .orElseThrow(
                        () -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Configured mapping target missing"));

        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        product.setFkStatus(rejectedStatus);
        product.setUpdatedAtUtc(nowUtc);

        Product savedProduct = productRepository.save(product);
        String adminEmail = authentication.getName() != null ? authentication.getName() : "admin@productadda.com";

        // TODO: Store rejection reason in separate audit or feedback table as per
        // checklist constraints

        return AdminProductApprovalResponseDto.builder()
                .productId(savedProduct.getPkProductId())
                .productName(savedProduct.getTitle())
                .status("REJECTED")
                .rejectionReason(request.getRejectionReason())
                .rejectedAtUtc(nowUtc.toString())
                .rejectedByAdmin(adminEmail)
                .build();
    }
}