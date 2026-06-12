/*
===============================================================================
Table       : users
Description :
    Core authentication and identity table for ProductAdda system.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS users
(
    pk_user_id       BINARY(16)      NOT NULL,

    first_name       VARCHAR(100)    NOT NULL,
    last_name        VARCHAR(100)    NOT NULL,

    email            VARCHAR(255)    NOT NULL,
    mobile           VARCHAR(20)     NULL,

    password_hash    VARCHAR(255)    NOT NULL,

    is_active        BOOLEAN         NOT NULL DEFAULT TRUE,

    created_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users_user_id
        PRIMARY KEY (pk_user_id),

    CONSTRAINT uq_users_email
        UNIQUE (email),

    CONSTRAINT uq_users_mobile
        UNIQUE (mobile)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_users_email
    ON users(email);

CREATE INDEX idx_users_mobile
    ON users(mobile);

CREATE INDEX idx_users_is_active
    ON users(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE users;

SHOW CREATE TABLE users;