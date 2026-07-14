package com.productadda.service.product;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import com.productadda.dto.product.CategoryResponseDto;

import com.productadda.repository.CategoryRepository;

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
public class ProductSearchGetAllCategoriesService {

    private final CategoryRepository categoryRepository;

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

}