package com.productadda.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_user_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkUserId;

    /*
     * =========================================================
     * USER INFO
     * =========================================================
     */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "mobile", unique = true, length = 20)
    private String mobile;

    /*
     * =========================================================
     * AUTH & OAUTH
     * =========================================================
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "google_id", unique = true, length = 255)
    private String googleId;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified;

    /*
     * =========================================================
     * STATUS & LOGINS
     * =========================================================
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "last_login_at_utc")
    private LocalDateTime lastLoginAtUtc;

    /*
     * =========================================================
     * AUDIT
     * =========================================================
     */
    @Column(name = "created_at_utc", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAtUtc;

    @Column(name = "updated_at_utc", nullable = false, insertable = false)
    private LocalDateTime updatedAtUtc;

    /*
     * =========================================================
     * RELATIONS
     * =========================================================
     */
    @OneToMany(mappedBy = "fkUser", fetch = FetchType.LAZY)
    private List<UserRole> userRoles;
}