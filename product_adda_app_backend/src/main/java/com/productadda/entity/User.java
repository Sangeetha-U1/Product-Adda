package com.productadda.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

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

    @Column(name = "mobile", length = 20)
    private String mobile;

    /*
     * =========================================================
     * AUTH
     * =========================================================
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

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
    @Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt;
}