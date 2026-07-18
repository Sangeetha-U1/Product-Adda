package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.InventoryAggregate;

public interface InventoryAggregateRepository extends JpaRepository<InventoryAggregate, UUID> {

    // ==========================================
    // MOST RECENTLY REFRESHED INVENTORY AGGREGATE ROW
    // Description: Used by materialized-view-status monitoring to determine
    // snapshot table freshness via the most recent updated_at_utc audit
    // column across all rows. Row counts are covered by JpaRepository's
    // built-in count().
    // ==========================================
    Optional<InventoryAggregate> findTopByOrderByUpdatedAtUtcDesc();
}
