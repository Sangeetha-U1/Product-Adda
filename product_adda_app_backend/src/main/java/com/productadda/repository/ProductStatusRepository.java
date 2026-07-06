package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.ProductStatus;

public interface ProductStatusRepository extends JpaRepository<ProductStatus, UUID> {

    Optional<ProductStatus> findByStatusCode(String statusCode);
}