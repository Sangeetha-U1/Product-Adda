package com.productadda.service.product;

import com.productadda.dto.product.ProductCreateRequestDto;
import com.productadda.dto.product.ProductResponseDto;
import com.productadda.entity.*;
import com.productadda.repository.*;
import com.productadda.util.UuidUtil;
import com.productadda.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductCreateService {

        private final ProductRepository productRepository;
        private final ProductStatusLookupRepository statusLookupRepository;
        private final CategoryRepository categoryRepository;
        private final BrandRepository brandRepository;
        private final UserRepository userRepository;
        private final VendorRepository vendorRepository;
        private final InventoryRepository inventoryRepository;
        private final UuidUtil uuidUtil;

        @Transactional
        public ProductResponseDto createProduct(ProductCreateRequestDto request) {
                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Performs rigorous state, input parameters and dependency
                 * verification checks
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (request == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Payload body is required and cannot be null");
                }
                if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Product name is required and cannot be blank");
                }
                if (request.getSkuCode() == null || request.getSkuCode().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Product SKU code is required and cannot be blank");
                }
                if (request.getCategoryId() == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Category ID is required");
                }
                if (request.getBrandId() == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Brand ID is required");
                }
                if (request.getPrice() == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Price is required");
                }
                if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Price must be a positive value greater than zero");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "User authentication context missing");
                }

                // Hardened programmatic security authority containment check
                boolean isVendor = authentication.getAuthorities().stream()
                                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_VENDOR")
                                                || grantedAuthority.getAuthority().equals("VENDOR"));

                if (!isVendor) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Access Denied: Security Principal context is missing required VENDOR permissions");
                }

                String currentUsername = authentication.getName();

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                User user = userRepository.findByEmail(currentUsername)
                                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                                                "Authenticated user identity record matching session metadata not found"));

                Vendor vendor = vendorRepository.findByFkUser(user)
                                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN,
                                                "Access Denied: Only active vendors can perform this operation"));

                if (vendor.getIsActive() == null || !vendor.getIsActive()) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Access Denied: Associated vendor profile status is currently inactive");
                }

                if (productRepository.existsBySku(request.getSkuCode())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Product SKU code '" + request.getSkuCode()
                                                        + "' already exists. SKU codes must be unique");
                }

                Category category = categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                                                "Specified category matching target ID does not exist"));

                Brand brand = brandRepository.findById(request.getBrandId())
                                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                                                "Specified brand matching target ID does not exist"));

                ProductStatusLookup initialStatus = statusLookupRepository.findByStatusCode("PENDING_APPROVAL")
                                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Target systemic lifecycle code configuration elements missing"));

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * Description: Constructs base managed references and maps operational states
                 * securely using custom UuidUtil
                 * ================================================================
                 */
                UUID targetProductId = uuidUtil.generateUuidV7();
                UUID targetInventoryId = uuidUtil.generateUuidV7();

                Product product = Product.builder()
                                .pkProductId(targetProductId)
                                .fkVendor(vendor)
                                .fkCategory(category)
                                .fkBrand(brand)
                                .fkStatus(initialStatus)
                                .title(request.getProductName())
                                .description(request.getDescription())
                                .sku(request.getSkuCode())
                                .price(request.getPrice())
                                .discountPrice(request.getDiscountPrice() != null ? request.getDiscountPrice()
                                                : BigDecimal.ZERO)
                                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                                .averageRating(BigDecimal.ZERO)
                                .totalReviews(0)
                                .isActive(true)
                                .build();

                Inventory inventory = Inventory.builder()
                                .pkInventoryId(targetInventoryId)
                                .fkProduct(product)
                                .availableQuantity(0)
                                .reservedQuantity(0)
                                .lowStockThreshold(5)
                                .isActive(true)
                                .build();

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * Description: Flushes operational objects down onto relational transactional
                 * structures
                 * ================================================================
                 */
                Product savedProduct = productRepository.save(product);
                inventoryRepository.save(inventory);

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * Description: Verification sequences and tracking context formatting checks
                 * ================================================================
                 */

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * Description: Wraps and renders output data securely inside isolated DTO
                 * elements
                 * ================================================================
                 */
                return ProductResponseDto.builder()
                                .productId(savedProduct.getPkProductId())
                                .vendorId(vendor.getPkVendorId())
                                .productName(savedProduct.getTitle())
                                .description(savedProduct.getDescription())
                                .skuCode(savedProduct.getSku())
                                .price(savedProduct.getPrice())
                                .discountPrice(savedProduct.getDiscountPrice())
                                .stockQuantity(savedProduct.getStockQuantity())
                                .categoryId(category.getPkCategoryId())
                                .brandId(brand.getPkBrandId())
                                .status(initialStatus.getStatusCode())
                                .createdAtUtc(
                                                java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
                                                                .format(DateTimeFormatter.ISO_INSTANT))
                                .build();
        }
}