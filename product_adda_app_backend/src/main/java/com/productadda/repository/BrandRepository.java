package com.productadda.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.productadda.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, UUID> {
    // Basic CRUD inherited from JpaRepository for lookup validation
}