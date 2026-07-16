package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.RecipientType;

public interface RecipientTypeRepository extends JpaRepository<RecipientType, UUID> {

    Optional<RecipientType> findByTypeName(String typeName);
}
