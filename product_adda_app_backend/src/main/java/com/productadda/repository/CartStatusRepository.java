package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.CartStatus;

public interface CartStatusRepository extends JpaRepository<CartStatus, UUID> {

    Optional<CartStatus> findByStatusCode(String statusCode);
}