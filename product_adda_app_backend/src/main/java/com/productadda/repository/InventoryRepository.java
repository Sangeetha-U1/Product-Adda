package com.productadda.repository;

import com.productadda.entity.Inventory;
import com.productadda.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByFkProduct(Product product);

    boolean existsByFkProduct(Product product);
}