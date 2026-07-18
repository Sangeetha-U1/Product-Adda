package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.SalesAggregate;

public interface SalesAggregateRepository extends JpaRepository<SalesAggregate, UUID> {

    // ==========================================
    // MOST RECENTLY REFRESHED SALES AGGREGATE ROW
    // Description: Used by materialized-view-status monitoring to determine
    // snapshot table freshness via the most recent updated_at_utc audit
    // column across all rows. Row counts are covered by JpaRepository's
    // built-in count().
    // ==========================================
    Optional<SalesAggregate> findTopByOrderByUpdatedAtUtcDesc();
}
