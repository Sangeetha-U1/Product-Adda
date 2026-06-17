/*
===============================================================================
Table       : address_types
Description :
Defines address classifications used across customer, vendor
and checkout modules.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS address_types;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE address_types
(
    pk_address_type_id BINARY(16) NOT NULL,

    address_type_name VARCHAR(50) NOT NULL,

    description VARCHAR(255) NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_address_types_address_type_id
        PRIMARY KEY (pk_address_type_id),

    CONSTRAINT uq_address_types_name
        UNIQUE (address_type_name)

);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_address_types_name
ON address_types(address_type_name);

CREATE INDEX idx_address_types_is_active
ON address_types(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE address_types;

SHOW CREATE TABLE address_types;
