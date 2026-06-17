/*
===============================================================================
Table       : users
Description :
    Core authentication and identity table.

    Stores all registered users in the system.

    Supports:
    - Customer accounts
    - Vendor accounts
    - Administrator accounts
    - Google Login
    - Authentication
    - Profile Management
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS users;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE users
(
    pk_user_id BINARY(16) NOT NULL,

    first_name VARCHAR(100) NOT NULL,

    last_name VARCHAR(100) NOT NULL,

    email VARCHAR(255) NOT NULL,

    mobile VARCHAR(20) NULL,

    password_hash VARCHAR(255) NOT NULL,

    google_id VARCHAR(255) NULL,

    email_verified BOOLEAN NOT NULL DEFAULT FALSE,

    last_login_at_utc TIMESTAMP NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_users_user_id
        PRIMARY KEY (pk_user_id),

    CONSTRAINT uq_users_email
        UNIQUE (email),

    CONSTRAINT uq_users_mobile
        UNIQUE (mobile),

    CONSTRAINT uq_users_google_id
        UNIQUE (google_id)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_users_email
    ON users(email);

CREATE INDEX idx_users_mobile
    ON users(mobile);

CREATE INDEX idx_users_google_id
    ON users(google_id);

CREATE INDEX idx_users_email_verified
    ON users(email_verified);

CREATE INDEX idx_users_is_active
    ON users(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE users;

SHOW CREATE TABLE users;