package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.AuditActionType;

public interface AuditActionTypeRepository extends JpaRepository<AuditActionType, UUID> {

    Optional<AuditActionType> findByActionTypeName(String actionTypeName);
}
