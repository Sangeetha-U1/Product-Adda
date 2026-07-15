package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.RecipientRole;

public interface RecipientRoleRepository extends JpaRepository<RecipientRole, UUID> {

    Optional<RecipientRole> findByRoleName(String roleName);
}
