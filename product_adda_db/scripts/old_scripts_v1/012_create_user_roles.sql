/*
===============================================================================
Table       : user_roles
Description :
    Stores user roles.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS user_roles
(
    pk_user_role_id BINARY(16) NOT NULL,

    fk_user_id      BINARY(16) NOT NULL,
    fk_role_id      BINARY(16) NOT NULL,

    created_at_utc  TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),

    CONSTRAINT pk_user_roles_user_role_id
        PRIMARY KEY (pk_user_role_id),

    CONSTRAINT fk_user_roles_user_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role_id
        FOREIGN KEY (fk_role_id)
        REFERENCES roles(pk_role_id)
        ON DELETE CASCADE,

    CONSTRAINT uq_user_roles_user_role
        UNIQUE (fk_user_id, fk_role_id)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_user_roles_fk_user_id
    ON user_roles(fk_user_id);

CREATE INDEX idx_user_roles_fk_role_id
    ON user_roles(fk_role_id);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE user_roles;

SHOW CREATE TABLE user_roles;