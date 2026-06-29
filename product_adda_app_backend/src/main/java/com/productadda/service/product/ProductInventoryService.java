package com.productadda.service.product;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

import com.productadda.dto.product.ProductInventoryRequestDto;
import com.productadda.dto.product.ProductInventoryResponseDto;
import com.productadda.dto.product.ProductUpdateInventoryRequestDto;
import com.productadda.dto.product.ProductUpdateInventoryResponseDto;

import com.productadda.entity.*;

import com.productadda.repository.*;

import com.productadda.util.UuidUtil;
import com.productadda.exception.ApiException;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductInventoryService {

        private final ProductRepository productRepository;
        private final InventoryRepository inventoryRepository;
        private final VendorRepository vendorRepository;
        private final UserRepository userRepository;
        private final UserRoleRepository userRoleRepository;

        private final UuidUtil uuidUtil;

        @Transactional
        public ProductInventoryResponseDto initializeInventory(UUID productId, ProductInventoryRequestDto request) {
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
                if (request.getCurrentStock() == null || request.getCurrentStock() < 0) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Current stock is required and cannot be negative");
                }
                if (request.getReorderLevel() == null || request.getReorderLevel() < 0) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Reorder level is required and cannot be negative");
                }
                if (request.getMaxStock() == null || request.getMaxStock() <= 0) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Maximum stock is required and must be positive");
                }
                if (request.getReorderLevel() > request.getMaxStock()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Reorder level (" + request.getReorderLevel()
                                                        + ") cannot exceed maximum stock ("
                                                        + request.getMaxStock() + ")");
                }
                if (request.getCurrentStock() > request.getMaxStock()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Current stock cannot exceed maximum stock");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "User authentication context missing");
                }

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
                                                "Authenticated user not found"));

                Vendor vendor = vendorRepository.findByFkUser(user)
                                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN,
                                                "Access Denied: Only active vendors can perform this operation"));

                if (vendor.getIsActive() == null || !vendor.getIsActive()) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Access Denied: Associated vendor profile status is currently inactive");
                }

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Product not found with id: " + productId));

                if (!product.getFkVendor().getPkVendorId().equals(vendor.getPkVendorId())) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Access Denied: You can only manage inventory for your own products");
                }

                if (inventoryRepository.existsByFkProduct(product)) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Inventory already initialized for this product. Use update endpoint.");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * Description: Entity construction and default value assignment
                 * ================================================================
                 */
                ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
                UUID inventoryId = uuidUtil.generateUuidV7();

                Inventory inventory = Inventory.builder()
                                .pkInventoryId(inventoryId)
                                .fkProduct(product)
                                .availableQuantity(request.getCurrentStock())
                                .reservedQuantity(0)
                                .lowStockThreshold(request.getReorderLevel())
                                .maxStock(request.getMaxStock())
                                .isActive(true)
                                .build();

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * Description: Persist the new inventory record
                 * ================================================================
                 */
                inventoryRepository.save(inventory);

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * Description: No sensitive data to mask in inventory response
                 * ================================================================
                 */

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * Description: Map entity to response DTO with computed fields
                 * ================================================================
                 */
                boolean isLowStock = inventory.getAvailableQuantity() <= inventory.getLowStockThreshold();

                return ProductInventoryResponseDto.builder()
                                .inventoryId(inventory.getPkInventoryId())
                                .productId(product.getPkProductId())
                                .currentStock(inventory.getAvailableQuantity())
                                .reorderLevel(inventory.getLowStockThreshold())
                                .maxStock(inventory.getMaxStock())
                                .isLowStock(isLowStock)
                                .availableForSale(inventory.getAvailableQuantity() > 0)
                                .createdAtUtc(now)
                                .updatedAtUtc(now)
                                .build();
        }

        @Transactional
        public ProductUpdateInventoryResponseDto updateInventory(UUID productId,
                        ProductUpdateInventoryRequestDto request) {
                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Partial update validation with ownership and existence checks
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (request == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Update request payload is required");
                }

                boolean hasAtLeastOneField = request.getCurrentStock().isPresent() ||
                                request.getReorderLevel().isPresent() ||
                                request.getMaxStock().isPresent();

                if (!hasAtLeastOneField) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "At least one field must be provided for update");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "User authentication context missing");
                }

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
                                                "Authenticated user not found"));

                Vendor vendor = vendorRepository.findByFkUser(user)
                                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN,
                                                "Access Denied: Only active vendors can perform this operation"));

                if (vendor.getIsActive() == null || !vendor.getIsActive()) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Access Denied: Associated vendor profile status is currently inactive");
                }

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Product not found with id: " + productId));

                if (!product.getFkVendor().getPkVendorId().equals(vendor.getPkVendorId())) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Access Denied: You can only manage inventory for your own products");
                }

                Inventory inventory = inventoryRepository.findByFkProduct(product)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Inventory not found for this product. Please initialize first."));

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * Description: Partial update using JsonNullable
                 * ================================================================
                 */
                Map<String, Object> updatedFields = new HashMap<>();

                if (request.getCurrentStock().isPresent()) {
                        Integer newStock = request.getCurrentStock().get();
                        if (newStock < 0) {
                                throw new ApiException(HttpStatus.BAD_REQUEST, "Current stock cannot be negative");
                        }
                        inventory.setAvailableQuantity(newStock);
                        updatedFields.put("currentStock", newStock);
                }

                if (request.getReorderLevel().isPresent()) {
                        Integer newReorder = request.getReorderLevel().get();
                        if (newReorder < 0) {
                                throw new ApiException(HttpStatus.BAD_REQUEST, "Reorder level cannot be negative");
                        }
                        inventory.setLowStockThreshold(newReorder);
                        updatedFields.put("reorderLevel", newReorder);
                }

                if (request.getMaxStock().isPresent()) {
                        Integer newMax = request.getMaxStock().get();
                        if (newMax <= 0) {
                                throw new ApiException(HttpStatus.BAD_REQUEST, "Maximum stock must be positive");
                        }
                        inventory.setMaxStock(newMax);
                        updatedFields.put("maxStock", newMax);
                }

                // Cross validation
                if (inventory.getLowStockThreshold() > inventory.getMaxStock()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Reorder level cannot exceed maximum stock");
                }
                if (inventory.getAvailableQuantity() > inventory.getMaxStock()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Current stock cannot exceed maximum stock");
                }

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * Description: Persist the updated inventory record
                 * ================================================================
                 */
                inventoryRepository.save(inventory);

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * Description: Return full state + updated fields
                 * ================================================================
                 */
                boolean isLowStock = inventory.getAvailableQuantity() <= inventory.getLowStockThreshold();
                ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

                ProductInventoryResponseDto fullInventory = ProductInventoryResponseDto.builder()
                                .inventoryId(inventory.getPkInventoryId())
                                .productId(product.getPkProductId())
                                .currentStock(inventory.getAvailableQuantity())
                                .reorderLevel(inventory.getLowStockThreshold())
                                .maxStock(inventory.getMaxStock())
                                .isLowStock(isLowStock)
                                .availableForSale(inventory.getAvailableQuantity() > 0)
                                .createdAtUtc(now)
                                .updatedAtUtc(now)
                                .build();

                return ProductUpdateInventoryResponseDto.builder()
                                .inventory(fullInventory)
                                .updatedFields(updatedFields)
                                .build();
        }

        @Transactional(readOnly = true)
        public ProductInventoryResponseDto getInventory(UUID productId) {
                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Dynamic authorization validation handling public access
                 * requirements
                 * alongside proactive token verification if a credential state exists.
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (productId == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Product ID must not be null");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                var authentication = SecurityContextHolder.getContext().getAuthentication();

                // Identify if the requester is an unauthenticated guest
                boolean isAnonymous = authentication == null
                                || !authentication.isAuthenticated()
                                || "anonymousUser".equals(authentication.getName());

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));

                String statusCode = product.getFkStatus().getStatusCode();
                boolean isApproved = "APPROVED".equals(statusCode);

                // CRITICAL: If a token IS passed, we MUST validate their DB status even if
                // the product is approved!
                User user = null;
                
                if (!isAnonymous) {
                        String currentUsername = authentication.getName();
                        user = userRepository.findByEmail(currentUsername)
                                        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                                                        "Authenticated user not found"));

                        // Real-time Database Cross-Verification of Roles (The Trusty Source)
                        List<UserRole> realTimeUserRoles = userRoleRepository.findByFkUser(user);
                        boolean hasDbVendorRole = realTimeUserRoles.stream()
                                        .map(userRole -> userRole.getFkRole().getRoleName().toUpperCase())
                                        .anyMatch("VENDOR"::equals);

                        // If they passed a token but their DB roles are wiped, deny them right here!
                        if (!hasDbVendorRole) {
                                throw new ApiException(HttpStatus.FORBIDDEN,
                                                "Access Denied: Account privileges have changed.");
                        }
                }

                // Content Governance Routing Rules
                if (!isApproved) {
                        // If the product is not approved, anonymous traffic is blocked immediately
                        if (isAnonymous) {
                                throw new ApiException(HttpStatus.NOT_FOUND, "Product not found");
                        }

                        // Double check blind context parameters synchronized out of the filter
                        boolean hasVendorContextRole = authentication.getAuthorities().stream()
                                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("VENDOR"));

                        if (!hasVendorContextRole) {
                                throw new ApiException(HttpStatus.NOT_FOUND, "Product not found");
                        }

                        // Vendor Profile Ownership Matching Rules (User is guaranteed to be non-null
                        // here)
                        Vendor vendor = vendorRepository.findByFkUser(user)
                                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));

                        if (!product.getFkVendor().getPkVendorId().equals(vendor.getPkVendorId())) {
                                throw new ApiException(HttpStatus.NOT_FOUND, "Product not found");
                        }
                }

                // Finalize entity validation lookup for the inventory tracking record
                Inventory inventory = inventoryRepository.findByFkProduct(product)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Inventory not configured for this product"));

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * Description: Compute derived state variables including low stock safety
                 * limits and stock status flags.
                 * ================================================================
                 */
                boolean isLowStock = inventory.getAvailableQuantity() <= inventory.getLowStockThreshold();
                boolean availableForSale = inventory.getAvailableQuantity() > 0;
                ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION (Skip if Read-Only GET)
                 * Description: Omitted because this represents a read-only GET pipeline.
                 * No state mutations or repository saves required.
                 * ================================================================
                 */

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * Description: Omitted because the inventory entity dataset does not retain
                 * PII or masked security field dependencies.
                 * ================================================================
                 */

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * Description: Build and safely output the formatted
                 * ProductInventoryResponseDto dataset.
                 * ================================================================
                 */
                return ProductInventoryResponseDto.builder()
                                .inventoryId(inventory.getPkInventoryId())
                                .productId(product.getPkProductId())
                                .currentStock(inventory.getAvailableQuantity())
                                .reorderLevel(inventory.getLowStockThreshold())
                                .maxStock(inventory.getMaxStock())
                                .isLowStock(isLowStock)
                                .availableForSale(availableForSale)
                                .createdAtUtc(now)
                                .updatedAtUtc(now)
                                .build();
        }
}