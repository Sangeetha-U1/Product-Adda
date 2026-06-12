package com.productadda.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.User;

public interface UserRepository
        extends JpaRepository<User, UUID> {

}