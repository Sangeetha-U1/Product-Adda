/*
===============================================================================
Table       : roles
Description :
Stores application roles used for authorization and access control
across ProductAdda modules.

Referenced by:
- user_roles
- Spring Security
- JWT Authorization
- Admin Module
- Vendor Module
- Customer Module

===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS roles;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE roles
(
    pk_role_id BINARY(16) NOT NULL,

    role_name VARCHAR(50) NOT NULL,

    description VARCHAR(255) NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

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

CREATE INDEX idx_roles_is_active
ON roles(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE roles;

SHOW CREATE TABLE roles;
