package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.DispatchStatus;

public interface DispatchStatusRepository extends JpaRepository<DispatchStatus, UUID> {

    Optional<DispatchStatus> findByStatusName(String statusName);
}
