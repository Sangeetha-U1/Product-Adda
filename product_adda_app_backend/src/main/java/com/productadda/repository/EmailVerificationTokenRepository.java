package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.entity.EmailVerificationToken;
import com.productadda.entity.User;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByVerificationToken(String verificationToken);

    Optional<EmailVerificationToken> findByFkUser(User fkUser);

    @Modifying
    @Transactional
    void deleteByFkUser(User fkUser); // Cleans up stale tokens if they try to register/resend again
}