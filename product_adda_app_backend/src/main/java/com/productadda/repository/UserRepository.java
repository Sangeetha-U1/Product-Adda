package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.User;

public interface UserRepository
        extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);

    Optional<User> findByEmail(String email);
}