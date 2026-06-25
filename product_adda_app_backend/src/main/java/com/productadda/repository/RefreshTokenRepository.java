package com.productadda.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.entity.RefreshToken;
import com.productadda.entity.User;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    Optional<RefreshToken> findByFkUser(User fkUser);

    @Modifying
    @Transactional
    void deleteByFkUser(User fkUser);

    List<RefreshToken> findAllByFkUserAndIsActiveTrue(User fkUser);
}