/*
===============================================================================
Table       : roles
Description :
    Stores roles.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS roles
(
    pk_role_id      BINARY(16)      NOT NULL,

    role_name       VARCHAR(50)     NOT NULL,
    description     VARCHAR(255)    NULL,

    created_at_utc  TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),

    CONSTRAINT pk_roles_role_id
        PRIMARY KEY (pk_role_id),

    CONSTRAINT uq_roles_role_name
        UNIQUE (role_name)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_roles_role_name
    ON roles(role_name);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE roles;

SHOW CREATE TABLE roles;