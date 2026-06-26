package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.ProductStatusLookup;

public interface ProductStatusLookupRepository extends JpaRepository<ProductStatusLookup, UUID> {

    Optional<ProductStatusLookup> findByStatusCode(String statusCode);
}