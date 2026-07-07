package com.productadda.repository;

import com.productadda.entity.InventoryReservation;
import com.productadda.entity.Product;
import com.productadda.entity.Cart;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {

    List<InventoryReservation> findByFkProductAndIsActiveTrue(Product product);

    List<InventoryReservation> findByFkCartAndIsActiveTrue(Cart cart);

    // TODO: Implement token search functionality when 'reservationToken' column is
    // added to schema
    // List<InventoryReservation> findByReservationToken(String reservationToken);
}