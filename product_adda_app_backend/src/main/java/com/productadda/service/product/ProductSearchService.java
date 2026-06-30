package com.productadda.service.product;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.List;
import java.math.BigDecimal;
import java.util.stream.Collectors;

import com.productadda.dto.product.CategoryResponseDto;
import com.productadda.dto.product.BrandResponseDto;
import com.productadda.dto.product.ProductSearchListResponseDto;
import com.productadda.dto.product.ProductSearchResponseDto;
import com.productadda.exception.ApiException;
import com.productadda.repository.CategoryRepository;
import com.productadda.repository.BrandRepository;
import com.productadda.repository.ProductRepository;
import com.productadda.entity.Product;

/**
 * =========================================================================
 * ### 🏗️ SERVICE : ProductSearchService
 * ### 📝 PURPOSE : Manages read-centric product exploration operations.
 * ### 📖 DESCRIPTION : Orchestrates product keyword searches, category list
 * generation,
 * brand identification, and complex database multi-criteria storefront matrix
 * filtering.
 * ### 🔒 VISIBILITY :
 * - **Access Modifier** : Public
 * - **Context** : Accessible by controller layers to serve public marketplace
 * requests and restricted administrator queries.
 * ### 📦 DEPENDENCIES :
 * - CategoryRepository : Fetches categorization classification groups.
 * - BrandRepository : Recovers commercial manufacturer brand records.
 * - ProductRepository : Handles advanced native lookups, pagination parameters,
 * and status checks.
 * ### 🛠️ CLASS TO DO : None - Architecture is highly optimized.
 * =========================================================================
 */
@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    /**
     * =========================================================================
     * ### ⚙️ METHOD : getAllCategories()
     * ### 📝 TITLE : Retrieve All Product Categories
     * ### 📖 DESCRIPTION : Scans database for catalog categories to supply client
     * discovery structures.
     * ### 🔒 VISIBILITY :
     * - **Access Modifier** : Public
     * - **Context** : Shared lookup called by public storefront menus. Inherits
     * global token context.
     * ### ⛓️ TRANSACTION :
     * - **Type** : None
     * - **Context** : Read-only pipeline fetching static definitions. No rollback
     * risks or entity mutations.
     * ### 📥 INPUTS : None
     * ### 📤 OUTPUTS :
     * - List<CategoryResponseDto> : Collection containing mapped category
     * identifiers, titles, and details.
     * ### 🚨 EXCEPTIONS : None
     * ### 🛠️ METHOD TODO: Integrate caching structures if category growth
     * scales up.
     * =========================================================================
     */
    public List<CategoryResponseDto> getAllCategories() {
        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: No input parameters or complex authority profiles required.
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        // Omitted: No parameter filters provided.

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        // Omitted: Categories are visible to public visitors.

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        // Omitted: Handled by structural list recovery fallbacks.

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        // Stream out all categories sequentially.

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * ================================================================
         */
        // Omitted: Method is strictly read-only.

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        // Omitted: Category records do not contain confidential fields.

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return categoryRepository.findAll().stream()
                .map(category -> CategoryResponseDto.builder()
                        .categoryId(category.getPkCategoryId())
                        .categoryName(category.getCategoryName())
                        .description(category.getCategoryDescription())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * =========================================================================
     * ### ⚙️ METHOD : getAllBrands()
     * ### 📝 TITLE : Retrieve All Corporate Brands
     * ### 📖 DESCRIPTION : Returns a collection of vendor brands listed across
     * active products.
     * ### 🔒 VISIBILITY :
     * - **Access Modifier** : Public
     * - **Context** : Publicly open route targeting search navigation components.
     * ### ⛓️ TRANSACTION :
     * - **Type** : None
     * - **Context** : Read-only execution mapping primary identifiers.
     * ### 📥 INPUTS : None
     * ### 📤 OUTPUTS :
     * - List<BrandResponseDto> : List containing structured manufacturer
     * identifiers and text values.
     * ### 🚨 EXCEPTIONS : None
     * ### 🛠️ METHOD TO DO: None - Code is production-ready.
     * =========================================================================
     */
    public List<BrandResponseDto> getAllBrands() {
        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: Basic fallback checks on catalog initialization.
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        // Omitted: Argument-free dataset acquisition.

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        // Omitted: Globally viewable by design.

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        // Omitted: Simple record extraction without input lookup filtering.

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        // Pull collection blocks from brand definitions.

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * ================================================================
         */
        // Omitted: Operation performs no database state alteration.

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        // Omitted: Brand profile structures do not hold sensitive variables.

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return brandRepository.findAll().stream()
                .map(brand -> BrandResponseDto.builder()
                        .brandId(brand.getPkBrandId())
                        .brandName(brand.getBrandName())
                        .logoUrl(null)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * =========================================================================
     * ### ⚙️ METHOD : searchProducts(String keyword, int page, int size)
     * ### 📝 TITLE : Paginated Keyword Inventory Search
     * ### 📖 DESCRIPTION : Performs structured keyword pattern matching across
     * titles, description blocks,
     * and specific item SKU values with dynamic visibility constraints based on
     * role metrics.
     * ### 🔒 VISIBILITY :
     * - **Access Modifier** : Public
     * - **Context** : Evaluates permissions to choose between full status
     * visibility or standard consumer limits.
     * ### ⛓️ TRANSACTION :
     * - **Type** : None
     * - **Context** : Completely isolated query execution tracking system catalog
     * indexes.
     * ### 📥 INPUTS :
     * - [String keyword] : Text term evaluated against indexed field items.
     * - [int page] : Offset tracking index value.
     * - [int size] : Numeric limitation metric restricting item limits per block.
     * ### 📤 OUTPUTS :
     * - ProductSearchListResponseDto : Consolidated object grouping results
     * collection with total page records.
     * ### 🚨 EXCEPTIONS :
     * - ApiException : Emitted for validation gaps like malformed search keywords
     * or page index values below boundaries.
     * ### 🛠️ METHOD TODO: Evaluate Full-Text Index optimizations if index
     * sizes increase.
     * =========================================================================
     */
    public ProductSearchListResponseDto searchProducts(String keyword, int page, int size) {
        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: Inspects query text layout and handles role assignments.
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Search keyword cannot be null or empty");
        }
        if (keyword.trim().length() < 2) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Search keyword must be at least 2 characters long");
        }
        if (page < 0 || size <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid pagination parameters provided");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        boolean isAdmin = false;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        // Omitted: Pattern verification occurs directly during extraction execution.

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        String matchTerm = "%" + keyword.trim() + "%";
        List<Product> matchedEntities;
        long totalCount;

        Pageable pageable = PageRequest.of(page, size);

        if (isAdmin) {
            matchedEntities = productRepository.searchAllStatuses(matchTerm, pageable);
            totalCount = productRepository.countSearchAllStatuses(matchTerm);
        } else {
            matchedEntities = productRepository.searchApprovedOnly(matchTerm, pageable);
            totalCount = productRepository.countSearchApprovedOnly(matchTerm);
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * ================================================================
         */
        // Omitted: Search route executes zero model update operations.

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        // Omitted: Content maps structural public item descriptions safely.

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        List<ProductSearchResponseDto> mappedResults = matchedEntities.stream()
                .map(p -> ProductSearchResponseDto.builder()
                        .productId(p.getPkProductId())
                        .productName(p.getTitle())
                        .description(p.getDescription())
                        .categoryName(p.getFkCategory() != null ? p.getFkCategory().getCategoryName() : null)
                        .brandName(p.getFkBrand() != null ? p.getFkBrand().getBrandName() : null)
                        .price(p.getPrice() != null ? p.getPrice().intValue() : 0)
                        .status(p.getFkStatus() != null ? p.getFkStatus().getStatusCode() : "PENDING")
                        .stock(p.getStockQuantity() != null ? p.getStockQuantity() : 0)
                        .build())
                .collect(Collectors.toList());

        return ProductSearchListResponseDto.builder()
                .products(mappedResults)
                .totalProducts(totalCount)
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    /**
     * =========================================================================
     * ### ⚙️ METHOD : filterProducts(UUID categoryId, UUID brandId, Integer
     * minPrice, Integer maxPrice, int page, int size)
     * ### 📝 TITLE : Dynamic Matrix Product Filtering
     * ### 📖 DESCRIPTION : Compiles a complex combination of category context
     * constraints, producer filters,
     * and specific pricing ranges into an optimized paginated search stream.
     * ### 🔒 VISIBILITY :
     * - **Access Modifier** : Public
     * - **Context** : Secures unapproved vendor items from public view by applying
     * user security clearance rules.
     * ### ⛓️ TRANSACTION :
     * - **Type** : None
     * - **Context** : Isolated lookups execution tracking structural query
     * parameters.
     * ### 📥 INPUTS :
     * - [UUID categoryId] : Target unique classification identifier (Optional).
     * - [UUID brandId] : Target manufacturer identifier profile (Optional).
     * - [Integer minPrice] : Floor limit parameter matching retail pricing.
     * - [Integer maxPrice] : Cap limit parameter matching retail pricing.
     * - [int page] : Active paging index tracker.
     * - [int size] : Document capacity scale definition limit.
     * ### 📤 OUTPUTS :
     * - ProductSearchListResponseDto : Configured compilation structure combining
     * subset outputs with index tracking fields.
     * ### 🚨 EXCEPTIONS :
     * - ApiException : Triggered by processing rules when price intervals cross
     * abnormally or targets are missing.
     * ### 🛠️ METHOD TO DO: None - Code is production-ready.
     * =========================================================================
     */
    public ProductSearchListResponseDto filterProducts(UUID categoryId, UUID brandId, Integer minPrice,
            Integer maxPrice, int page, int size) {
        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: Ensures metric limits match and contextual profiles exist.
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (page < 0 || size <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid pagination parameters provided");
        }
        if (minPrice != null && minPrice < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Minimum price cannot be negative");
        }
        if (maxPrice != null && maxPrice < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Maximum price cannot be negative");
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Minimum price (" + minPrice + ") cannot be greater than maximum price (" + maxPrice + ")");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        boolean isAdmin = false;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Target filtering category context does not exist");
        }
        if (brandId != null && !brandRepository.existsById(brandId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Target filtering brand context does not exist");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        BigDecimal minPriceBd = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal maxPriceBd = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;

        List<Product> matchedEntities;
        long totalCount;

        Pageable pageable = PageRequest.of(page, size);

        if (isAdmin) {
            matchedEntities = productRepository.filterAllStatuses(categoryId, brandId, minPriceBd, maxPriceBd,
                    pageable);
            totalCount = productRepository.countFilterAllStatuses(categoryId, brandId, minPriceBd, maxPriceBd);
        } else {
            matchedEntities = productRepository.filterApprovedOnly(categoryId, brandId, minPriceBd, maxPriceBd,
                    pageable);
            totalCount = productRepository.countFilterApprovedOnly(categoryId, brandId, minPriceBd, maxPriceBd);
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * ================================================================
         */
        // Omitted: Read-only lookup containing zero storage write calls.

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        // Omitted: Elements contain structural public records.

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        List<ProductSearchResponseDto> mappedResults = matchedEntities.stream()
                .map(p -> ProductSearchResponseDto.builder()
                        .productId(p.getPkProductId())
                        .productName(p.getTitle())
                        .description(p.getDescription())
                        .categoryName(p.getFkCategory() != null ? p.getFkCategory().getCategoryName() : null)
                        .brandName(p.getFkBrand() != null ? p.getFkBrand().getBrandName() : null)
                        .price(p.getPrice() != null ? p.getPrice().intValue() : 0)
                        .status(p.getFkStatus() != null ? p.getFkStatus().getStatusCode() : "PENDING")
                        .stock(p.getStockQuantity() != null ? p.getStockQuantity() : 0)
                        .build())
                .collect(Collectors.toList());

        return ProductSearchListResponseDto.builder()
                .products(mappedResults)
                .totalProducts(totalCount)
                .currentPage(page)
                .pageSize(size)
                .build();
    }
}