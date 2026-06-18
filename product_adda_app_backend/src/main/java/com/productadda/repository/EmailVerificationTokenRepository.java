package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.productadda.entity.EmailVerificationToken;
import com.productadda.entity.User;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByVerificationToken(String verificationToken);

    Optional<EmailVerificationToken> findByFkUser(User fkUser);

    @Modifying
    void deleteByFkUser(User fkUser);
}