package com.productadda.service.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.product.AdminModerationStatsResponseDto;
import com.productadda.dto.product.AdminProductPendingDataWrapper;
import com.productadda.dto.product.AdminProductPendingListResponseDto;
import com.productadda.entity.Product;
import com.productadda.exception.ApiException;
import com.productadda.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminProductModerationService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public AdminProductPendingDataWrapper getPendingProducts(int page, int size) {
        if (page < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Page index parameters must not be negative indices");
        }
        if (size <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Page batch size parameter must be greater than zero");
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

        Pageable pageable = PageRequest.of(page, size);
        List<String> pendingCodes = List.of("PENDING_APPROVAL", "DRAFT", "REJECTED");
        Page<Product> productPage = productRepository.findByStatusCodeInNative(pendingCodes, pageable);

        List<AdminProductPendingListResponseDto> mappedProducts = productPage
                .map(product -> AdminProductPendingListResponseDto.builder()
                        .productId(product.getPkProductId())
                        .productName(product.getTitle())
                        .description(product.getDescription())
                        .vendorId(product.getFkVendor() != null ? product.getFkVendor().getPkVendorId() : null)
                        .vendorName(product.getFkVendor() != null ? product.getFkVendor().getBusinessName() : null)
                        .categoryName(
                                product.getFkCategory() != null ? product.getFkCategory().getCategoryName() : null)
                        .brandName(product.getFkBrand() != null ? product.getFkBrand().getBrandName() : null)
                        .price(product.getPrice())
                        .status(product.getFkStatus() != null ? product.getFkStatus().getStatusCode() : "UNKNOWN")
                        .createdAtUtc(product.getCreatedAtUtc() != null ? product.getCreatedAtUtc().toString() : null)
                        .build())
                .getContent();

        long totalPendingCount = productRepository.countByStatusCodeNative("PENDING_APPROVAL");

        return AdminProductPendingDataWrapper.builder()
                .products(mappedProducts)
                .totalPendingProducts(totalPendingCount)
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminModerationStatsResponseDto getModerationStatistics() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Security context is missing raw authentication data");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SUPER_ADMIN") || a.getAuthority().equals("ADMIN"));
        if (!isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied: Only admins can access moderation endpoints");
        }

        LocalDateTime startOfToday = LocalDate.now(ZoneOffset.UTC).atStartOfDay();

        long totalPending = productRepository.countByStatusCodeNative("PENDING_APPROVAL");
        long approvedToday = productRepository.countByStatusCodeAndUpdatedAtGreaterNative("APPROVED", startOfToday);
        long rejectedToday = productRepository.countByStatusCodeAndUpdatedAtGreaterNative("REJECTED", startOfToday);
        long pendingVendorsCount = productRepository.countDistinctVendorsByStatusCodeNative("PENDING_APPROVAL");

        return AdminModerationStatsResponseDto.builder()
                .totalPendingProducts(totalPending)
                .approvedToday(approvedToday)
                .rejectedToday(rejectedToday)
                .pendingVendorsCount(pendingVendorsCount)
                .averageApprovalTimeHours(4.5)
                .lastUpdateUtc(LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }
}