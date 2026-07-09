package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.ItemStatus;

public interface ItemStatusRepository extends JpaRepository<ItemStatus, UUID> {

    Optional<ItemStatus> findByStatusName(String statusName);

}