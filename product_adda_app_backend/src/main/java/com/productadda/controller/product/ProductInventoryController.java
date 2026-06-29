package com.productadda.controller.product;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import com.productadda.dto.product.ProductInventoryRequestDto;
import com.productadda.dto.product.ProductInventoryResponseDto;
import com.productadda.dto.product.ProductUpdateInventoryRequestDto;
import com.productadda.dto.product.ProductUpdateInventoryResponseDto;
import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.service.product.ProductInventoryService;

/**
 * =========================================================================
 * ### 🎮 CONTROLLER : ProductInventoryController
 * ### 📝 BASE PATH : /api
 * ### 📖 DESCRIPTION : Gateway controller responsible for handling ingress REST
 * requests
 * governing product inventory allocations, stock modifications,
 * and availability telemetry data queries.
 * * ### 🔒 VISIBILITY :
 * - **Access Modifier** : Public
 * - **Context** : Exposed globally as an API Web Endpoint layer. Routes are
 * split between
 * publicly whitelisted storefront read routes and secure, role-restricted
 * vendor write mutations.
 * * ### 📦 DEPENDENCIES :
 * - `ProductInventoryService` : Business logic orchestration engine managing
 * transaction logic and domain persistence rules.
 * * ### 🛠️ CLASS TO DO : None - Architecture is highly optimized and
 * production-ready.
 * =========================================================================
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductInventoryController {

        private final ProductInventoryService productInventoryService;

        /**
         * =========================================================================
         * ### 🎯 ENDPOINT : POST
         * http://localhost:8080/api/products/{productId}/inventory
         * ### 📝 TITLE : Initialize Product Inventory Record
         * ### 📖 DESCRIPTION : Provisions a brand new stock and tracking allocation
         * record for a newly registered product.
         * * ### 🔒 SECURITY :
         * - **Visibility** : Private
         * - **Authority** : `hasAnyAuthority('VENDOR')`
         * - **Context** : Restricted to authenticated vendor profiles to prevent rogue
         * initialization or external data pollution. Enforced at runtime via Spring
         * Method Security.
         * * ### 📥 REQUESTS :
         * - `@PathVariable UUID productId` : Unique database identification mapping
         * back to the baseline product master record.
         * - `@RequestBody ProductInventoryRequestDto request` : Payload passing in
         * original warehouse stock quantity, limits, and allocation rules.
         * * ### 📤 RESPONSES :
         * - `201 CREATED` : Core inventory metadata was mapped, stored, and bound
         * safely to the entity context.
         * - `400 BAD REQUEST` : Triggered if input payload validation checks fail or
         * data structure is corrupt.
         * - `401 UNAUTHORIZED` : Missing or entirely malformed bearer session token
         * header.
         * - `403 FORBIDDEN` : The caller is authenticated but holds an unauthorized
         * platform privilege level.
         * * ### 🛠️ TO DO : None - Code is production-ready.
         * =========================================================================
         */
        @PostMapping("/products/{productId}/inventory")
        @PreAuthorize("hasAnyAuthority('VENDOR')")
        public ResponseEntity<ApiSuccessResponseDto<ProductInventoryResponseDto>> initializeInventory(
                        @PathVariable UUID productId,
                        @RequestBody ProductInventoryRequestDto request) {

                ProductInventoryResponseDto responseData = productInventoryService
                                .initializeInventory(productId, request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiSuccessResponseDto.<ProductInventoryResponseDto>builder()
                                                .success(true)
                                                .message("Product inventory initialized successfully")
                                                .data(responseData)
                                                .build());
        }

        /**
         * =========================================================================
         * ### 🎯 ENDPOINT : PUT
         * http://localhost:8080/api/products/{productId}/inventory
         * ### 📝 TITLE : Update Product Inventory Allocations
         * ### 📖 DESCRIPTION : Modifies active warehouse quantities, re-allocates
         * structural buffers, or alters inventory configuration thresholds.
         * * ### 🔒 SECURITY :
         * - **Visibility** : Private
         * - **Authority** : `hasAnyAuthority('VENDOR')`
         * - **Context** : Requires active vendor permissions. Validates that incoming
         * supply variations map onto an established resource sequence.
         * * ### 📥 REQUESTS :
         * - `@PathVariable UUID productId` : The database unique tracking ID of the
         * target resource.
         * - `@RequestBody ProductInventoryRequestDto request` : Overwrite structural
         * request containing updated values for fields such as stock quantities.
         * * ### 📤 RESPONSES :
         * - `200 OK` : System successfully overrode underlying inventory state data
         * records.
         * - `400 BAD REQUEST` : Request formatting issues or logic checks (e.g., net
         * negative physical counts) violated.
         * - `401 UNAUTHORIZED` : The security authentication context session token is
         * invalid or missing.
         * - `403 FORBIDDEN` : Logged-in profile lacks necessary vendor administrative
         * access credentials.
         * - `404 NOT FOUND` : Triggered deep within service dependencies if no
         * pre-existing stock layout traces back to the productId.
         * * ### 🛠️ TO DO : None - Code is production-ready.
         * =========================================================================
         */
        @PutMapping("/products/{productId}/inventory")
        @PreAuthorize("hasAnyAuthority('VENDOR')")
        public ResponseEntity<ApiSuccessResponseDto<ProductUpdateInventoryResponseDto>> updateInventory(
                        @PathVariable UUID productId,
                        @RequestBody ProductUpdateInventoryRequestDto request) {

                ProductUpdateInventoryResponseDto responseData = productInventoryService
                                .updateInventory(productId, request);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<ProductUpdateInventoryResponseDto>builder()
                                                .success(true)
                                                .message("Product inventory updated successfully")
                                                .data(responseData)
                                                .build());
        }

        /**
         * =========================================================================
         * ### 🎯 ENDPOINT : GET
         * http://localhost:8080/api/products/{productId}/inventory
         * ### 📝 TITLE : Fetch Current Product Inventory Metrics
         * ### 📖 DESCRIPTION : Pulls active product counts and public-facing stock
         * visibility constraints for downstream layout display.
         * * ### 🔒 SECURITY :
         * - **Visibility** : Public
         * - **Authority** : None - Accessible to all users
         * - **Context** : Whitelisted in public security routing profiles to allow
         * anonymous checkout, guest browsing, and microservice queries to inspect item
         * availability constraints seamlessly.
         * * ### 📥 REQUESTS :
         * - `@PathVariable UUID productId` : Unique system identifier pinpointing the
         * requested product entity.
         * * ### 📤 RESPONSES :
         * - `200 OK` : Product inventory profile records were located and successfully
         * wrapped inside the response envelope.
         * - `404 NOT FOUND` : Thrown if product lookup mapping results in a null
         * database entity lookup.
         * * ### 🛠️ TO DO : None - Code is production-ready.
         * =========================================================================
         */
        @GetMapping("/products/{productId}/inventory")
        public ResponseEntity<ApiSuccessResponseDto<ProductInventoryResponseDto>> getInventory(
                        @PathVariable UUID productId) {

                ProductInventoryResponseDto responseData = productInventoryService.getInventory(productId);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<ProductInventoryResponseDto>builder()
                                                .success(true)
                                                .message("Product inventory retrieved successfully")
                                                .data(responseData)
                                                .build());
        }
}