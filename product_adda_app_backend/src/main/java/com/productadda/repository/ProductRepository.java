package com.productadda.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Product;

public interface ProductRepository
        extends JpaRepository<Product, UUID> {

}