package com.productadda.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

        /*
         * =========================================================
         * PRIMARY KEY
         * =========================================================
         */
        @Id
        @JdbcTypeCode(SqlTypes.BINARY)
        @Column(name = "pk_refresh_token_id", columnDefinition = "BINARY(16)", nullable = false)
        private UUID pkRefreshTokenId;

        /*
         * =========================================================
         * FOREIGN KEY → USERS
         * =========================================================
         */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "fk_user_id", nullable = false)
        private User fkUser;

        @Column(name = "token_family_id", columnDefinition = "BINARY(16)", nullable = false)
        private UUID tokenFamilyId;

        /*
         * =========================================================
         * TOKEN HASH
         * =========================================================
         */
        @Column(name = "token_hash", nullable = false, unique = true, length = 255)
        private String tokenHash;

        /*
         * =========================================================
         * EXPIRY
         * =========================================================
         */
        @Column(name = "expires_at_utc", nullable = false)
        private LocalDateTime expiresAtUtc;

        /*
         * =========================================================
         * REVOCATION
         * =========================================================
         */
        @Column(name = "revoked_at_utc")
        private LocalDateTime revokedAtUtc;

        /*
         * =========================================================
         * STATUS
         * =========================================================
         */
        @Column(name = "is_active", nullable = false)
        private Boolean isActive;

        /*
         * =========================================================
         * AUDIT
         * =========================================================
         */
        @Column(name = "created_at_utc", nullable = false, insertable = false, updatable = false)
        private LocalDateTime createdAtUtc;

        @Column(name = "updated_at_utc", nullable = false, insertable = false)
        private LocalDateTime updatedAtUtc;
}