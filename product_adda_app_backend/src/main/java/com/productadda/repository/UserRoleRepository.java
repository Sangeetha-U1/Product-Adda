package com.productadda.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.entity.Role;

public interface UserRoleRepository
        extends JpaRepository<UserRole, UUID> {

    boolean existsByFkUserAndFkRole(User fkUser, Role fkRole);

    boolean existsByFkUserAndFkRole_RoleName(User fkUser, String roleName);

    List<UserRole> findByFkUser(User user);
}