package com.productadda.service.product;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import com.productadda.dto.product.ProductSearchListResponseDto;
import com.productadda.dto.product.ProductSearchResponseDto;

import com.productadda.exception.ApiException;

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
public class ProductSearchSearchProductsService {

    private final ProductRepository productRepository;

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
                    .anyMatch(a -> a.getAuthority().equals("SUPER_ADMIN"));
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