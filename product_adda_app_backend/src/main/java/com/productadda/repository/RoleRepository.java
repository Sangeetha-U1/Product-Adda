package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Role;

public interface RoleRepository
        extends JpaRepository<Role, UUID> {

    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);
}