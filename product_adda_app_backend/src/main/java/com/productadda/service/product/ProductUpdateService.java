package com.productadda.service.product;

import com.productadda.dto.product.ProductResponseDto;
import com.productadda.dto.product.ProductUpdateResponseDto;
import com.productadda.dto.product.ProductUpdateRequestDto;

import com.productadda.entity.Brand;
import com.productadda.entity.Category;
import com.productadda.entity.Product;
import com.productadda.entity.User;
import com.productadda.entity.Vendor;

import com.productadda.exception.ApiException;

import com.productadda.repository.BrandRepository;
import com.productadda.repository.CategoryRepository;
import com.productadda.repository.ProductRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductUpdateService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;

    @Transactional
    public ProductUpdateResponseDto updateProduct(UUID productId, ProductUpdateRequestDto request) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: Zero-Trust validation for partial product update using
         * JsonNullable
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Update request payload is required");
        }

        boolean hasAtLeastOneField = request.getProductName().isPresent() ||
                request.getDescription().isPresent() ||
                request.getSkuCode().isPresent() ||
                request.getCategoryId().isPresent() ||
                request.getBrandId().isPresent() ||
                request.getPrice().isPresent() ||
                request.getDiscountPrice().isPresent();

        if (!hasAtLeastOneField) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "At least one field must be provided for update");
        }

        // Individual field validation
        if (request.getProductName().isPresent()) {
            String name = request.getProductName().get();
            if (name == null || name.trim().isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Product name cannot be blank");
            }
        }

        if (request.getDescription().isPresent()) {
            String desc = request.getDescription().get();
            if (desc == null || desc.trim().isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Product description cannot be blank");
            }
        }

        if (request.getSkuCode().isPresent()) {
            String sku = request.getSkuCode().get();
            if (sku == null || sku.trim().isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Product SKU code cannot be blank");
            }
        }

        if (request.getPrice().isPresent()) {
            BigDecimal price = request.getPrice().get();
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Product price must be a positive value");
            }
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User authentication context missing");
        }

        boolean isVendor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR") || a.getAuthority().equals("VENDOR"));

        if (!isVendor) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied: Only vendors can update products");
        }

        String currentUsername = authentication.getName();

        User user = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        Vendor vendor = vendorRepository.findByFkUser(user)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Vendor profile not found"));

        if (vendor.getIsActive() == null || !vendor.getIsActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied: Vendor account is inactive");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Product not found with ID: " + productId));

        if (!product.getFkVendor().getPkVendorId().equals(vendor.getPkVendorId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied: You can only update your own products");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING (Partial Update)
         * ================================================================
         */
        Map<String, Object> updatedFields = new HashMap<>();

        if (request.getProductName().isPresent()) {
            String name = request.getProductName().get();
            product.setTitle(name);
            updatedFields.put("productName", name);
        }

        if (request.getDescription().isPresent()) {
            String desc = request.getDescription().get();
            product.setDescription(desc);
            updatedFields.put("description", desc);
        }

        if (request.getSkuCode().isPresent()) {
            String sku = request.getSkuCode().get();
            if (!product.getSku().equals(sku)) {
                if (productRepository.existsBySku(sku)) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Product SKU code '" + sku + "' already exists.");
                }
            }
            product.setSku(sku);
            updatedFields.put("skuCode", sku);
        }

        if (request.getPrice().isPresent()) {
            BigDecimal price = request.getPrice().get();
            product.setPrice(price);
            updatedFields.put("price", price);
        }

        if (request.getDiscountPrice().isPresent()) {
            BigDecimal discount = request.getDiscountPrice().get();
            product.setDiscountPrice(discount);
            updatedFields.put("discountPrice", discount);
        }

        if (request.getCategoryId().isPresent()) {
            UUID catId = request.getCategoryId().get();
            Category category = categoryRepository.findById(catId)
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Category not found"));
            product.setFkCategory(category);
            updatedFields.put("categoryId", catId);
        }

        if (request.getBrandId().isPresent()) {
            UUID brandId = request.getBrandId().get();
            Brand brand = brandRepository.findById(brandId)
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Brand not found"));
            product.setFkBrand(brand);
            updatedFields.put("brandId", brandId);
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        Product savedProduct = productRepository.save(product);

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        ProductResponseDto fullProduct = ProductResponseDto.builder()
                .productId(savedProduct.getPkProductId())
                .vendorId(vendor.getPkVendorId())
                .productName(savedProduct.getTitle())
                .description(savedProduct.getDescription())
                .skuCode(savedProduct.getSku())
                .categoryId(
                        savedProduct.getFkCategory() != null ? savedProduct.getFkCategory().getPkCategoryId() : null)
                .brandId(savedProduct.getFkBrand() != null ? savedProduct.getFkBrand().getPkBrandId() : null)
                .price(savedProduct.getPrice())
                .discountPrice(savedProduct.getDiscountPrice())
                .status(savedProduct.getFkStatus() != null ? savedProduct.getFkStatus().getStatusCode() : null)
                .createdAtUtc(java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT))
                .build();

        return ProductUpdateResponseDto.builder()
                .product(fullProduct)
                .updatedFields(updatedFields)
                .build();
    }
}