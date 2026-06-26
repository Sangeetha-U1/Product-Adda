package com.productadda.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Product;
import com.productadda.entity.Vendor;

public interface ProductRepository extends JpaRepository<Product, UUID> {

        // ==========================================
        // VENDOR METRICS QUERY
        // Description: Counts all product listings
        // belonging to a specific vendor identity.
        // ==========================================
        long countByFkVendor(Vendor vendor);

        boolean existsBySku(String sku);

        Page<Product> findByFkVendor(Vendor vendor, Pageable pageable);
}