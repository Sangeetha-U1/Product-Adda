/*
===============================================================================
Table       : users
Description :
    Stores basic user information for customers, vendors, and admins.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS users
(
    user_id          BIGINT AUTO_INCREMENT NOT NULL,

    first_name       VARCHAR(100)          NOT NULL,
    last_name        VARCHAR(100)          NOT NULL,
    email            VARCHAR(255)          NOT NULL,
    mobile           VARCHAR(20)           NULL,

    CONSTRAINT pk_users
        PRIMARY KEY (user_id),

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

-- ============================================================================
-- Table Verification
-- ============================================================================

DESCRIBE users;

SHOW CREATE TABLE users;