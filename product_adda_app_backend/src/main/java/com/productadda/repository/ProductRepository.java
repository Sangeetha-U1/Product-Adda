package com.productadda.repository;

import java.util.UUID;
import java.util.List;
import java.math.BigDecimal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.productadda.entity.Product;
import com.productadda.entity.Vendor;

public interface ProductRepository extends JpaRepository<Product, UUID> {

        long countByFkVendor(Vendor vendor);

        boolean existsBySku(String sku);

        Page<Product> findByFkVendor(Vendor vendor, Pageable pageable);

        // =========================================================================
        // KEYWORD SEARCH NATIVE QUERIES (Bypasses VS Code limit validation using
        // Pageable)
        // =========================================================================
        @Query(value = "SELECT p.* FROM products p " +
                        "LEFT JOIN product_status_lookup s ON p.fk_status_id = s.pk_status_id " +
                        "WHERE (p.title LIKE :keyword OR p.description LIKE :keyword OR p.sku LIKE :keyword)", nativeQuery = true)
        List<Product> searchAllStatuses(@Param("keyword") String keyword, Pageable pageable);

        @Query(value = "SELECT COUNT(*) FROM products p " +
                        "WHERE (p.title LIKE :keyword OR p.description LIKE :keyword OR p.sku LIKE :keyword)", nativeQuery = true)
        long countSearchAllStatuses(@Param("keyword") String keyword);

        @Query(value = "SELECT p.* FROM products p " +
                        "JOIN product_status_lookup s ON p.fk_status_id = s.pk_status_id " +
                        "WHERE (p.title LIKE :keyword OR p.description LIKE :keyword OR p.sku LIKE :keyword) " +
                        "AND s.status_code = 'APPROVED'", nativeQuery = true)
        List<Product> searchApprovedOnly(@Param("keyword") String keyword, Pageable pageable);

        @Query(value = "SELECT COUNT(*) FROM products p " +
                        "JOIN product_status_lookup s ON p.fk_status_id = s.pk_status_id " +
                        "WHERE (p.title LIKE :keyword OR p.description LIKE :keyword OR p.sku LIKE :keyword) " +
                        "AND s.status_code = 'APPROVED'", nativeQuery = true)
        long countSearchApprovedOnly(@Param("keyword") String keyword);

        // =========================================================================
        // MULTI-CRITERIA FILTER NATIVE QUERIES
        // =========================================================================
        @Query(value = "SELECT p.* FROM products p " +
                        "LEFT JOIN product_status_lookup s ON p.fk_status_id = s.pk_status_id " +
                        "WHERE (:categoryId IS NULL OR p.fk_category_id = :categoryId) " +
                        "AND (:brandId IS NULL OR p.fk_brand_id = :brandId) " +
                        "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.price <= :maxPrice)", nativeQuery = true)
        List<Product> filterAllStatuses(@Param("categoryId") UUID categoryId, @Param("brandId") UUID brandId,
                        @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice,
                        Pageable pageable);

        @Query(value = "SELECT COUNT(*) FROM products p " +
                        "WHERE (:categoryId IS NULL OR p.fk_category_id = :categoryId) " +
                        "AND (:brandId IS NULL OR p.fk_brand_id = :brandId) " +
                        "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.price <= :maxPrice)", nativeQuery = true)
        long countFilterAllStatuses(@Param("categoryId") UUID categoryId, @Param("brandId") UUID brandId,
                        @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

        @Query(value = "SELECT p.* FROM products p " +
                        "JOIN product_status_lookup s ON p.fk_status_id = s.pk_status_id " +
                        "WHERE s.status_code = 'APPROVED' " +
                        "AND (:categoryId IS NULL OR p.fk_category_id = :categoryId) " +
                        "AND (:brandId IS NULL OR p.fk_brand_id = :brandId) " +
                        "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.price <= :maxPrice)", nativeQuery = true)
        List<Product> filterApprovedOnly(@Param("categoryId") UUID categoryId, @Param("brandId") UUID brandId,
                        @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice,
                        Pageable pageable);

        @Query(value = "SELECT COUNT(*) FROM products p " +
                        "JOIN product_status_lookup s ON p.fk_status_id = s.pk_status_id " +
                        "WHERE s.status_code = 'APPROVED' " +
                        "AND (:categoryId IS NULL OR p.fk_category_id = :categoryId) " +
                        "AND (:brandId IS NULL OR p.fk_brand_id = :brandId) " +
                        "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.price <= :maxPrice)", nativeQuery = true)
        long countFilterApprovedOnly(@Param("categoryId") UUID categoryId, @Param("brandId") UUID brandId,
                        @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
}