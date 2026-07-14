package com.productadda.controller.product;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.product.CategoryResponseDto;
import com.productadda.dto.product.BrandResponseDto;
import com.productadda.dto.product.ProductSearchListResponseDto;

import com.productadda.service.product.ProductSearchFilterProductsService;
import com.productadda.service.product.ProductSearchGetAllBrandsService;
import com.productadda.service.product.ProductSearchGetAllCategoriesService;
import com.productadda.service.product.ProductSearchSearchProductsService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductSearchController {

        private final ProductSearchGetAllCategoriesService productSearchGetAllCategoriesService;
        private final ProductSearchGetAllBrandsService productSearchGetAllBrandsService;
        private final ProductSearchSearchProductsService productSearchSearchProductsService;
        private final ProductSearchFilterProductsService productSearchFilterProductsService;

        @GetMapping("/categories")
        public ResponseEntity<ApiSuccessResponseDto<Map<String, Object>>> getCategories() {
                List<CategoryResponseDto> categories = productSearchGetAllCategoriesService.getAllCategories();

                Map<String, Object> data = new HashMap<>();
                data.put("categories", categories);
                data.put("totalCategories", categories.size());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<Map<String, Object>>builder()
                                                .success(true)
                                                .message("Product categories retrieved successfully")
                                                .data(data)
                                                .build());
        }

        @GetMapping("/brands")
        public ResponseEntity<ApiSuccessResponseDto<Map<String, Object>>> getBrands() {
                List<BrandResponseDto> brands = productSearchGetAllBrandsService.getAllBrands();

                Map<String, Object> data = new HashMap<>();
                data.put("brands", brands);
                data.put("totalBrands", brands.size());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<Map<String, Object>>builder()
                                                .success(true)
                                                .message("Brands retrieved successfully")
                                                .data(data)
                                                .build());
        }

        @GetMapping("/search")
        public ResponseEntity<ApiSuccessResponseDto<ProductSearchListResponseDto>> searchProducts(
                        @RequestParam String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                ProductSearchListResponseDto searchResult = productSearchSearchProductsService.searchProducts(keyword, page, size);

                String successMsg = searchResult.getProducts().isEmpty()
                                ? "No products found matching keyword '" + keyword + "'"
                                : "Products found matching keyword '" + keyword + "'";

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<ProductSearchListResponseDto>builder()
                                                .success(true)
                                                .message(successMsg)
                                                .data(searchResult)
                                                .build());
        }

        @GetMapping("/filter")
        public ResponseEntity<ApiSuccessResponseDto<ProductSearchListResponseDto>> filterProducts(
                        @RequestParam(required = false) UUID categoryId,
                        @RequestParam(required = false) UUID brandId,
                        @RequestParam(required = false) Integer minPrice,
                        @RequestParam(required = false) Integer maxPrice,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                ProductSearchListResponseDto filterResult = productSearchFilterProductsService.filterProducts(categoryId, brandId,
                                minPrice, maxPrice, page, size);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<ProductSearchListResponseDto>builder()
                                                .success(true)
                                                .message("Products filtered successfully")
                                                .data(filterResult)
                                                .build());
        }
}