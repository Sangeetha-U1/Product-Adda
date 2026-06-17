/*
===============================================================================
Table : inventory_transaction_types
Description :
Defines inventory movement types used by inventory management
and reporting modules.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS inventory_transaction_types;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE inventory_transaction_types
(
    pk_transaction_type_id BINARY(16) NOT NULL,

    transaction_type_name VARCHAR(50) NOT NULL,

    description VARCHAR(255) NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_inventory_transaction_types_transaction_type_id
        PRIMARY KEY (pk_transaction_type_id),

    CONSTRAINT uq_inventory_transaction_types_name
        UNIQUE (transaction_type_name)

);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_inventory_transaction_types_name
ON inventory_transaction_types(transaction_type_name);

CREATE INDEX idx_inventory_transaction_types_is_active
ON inventory_transaction_types(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE inventory_transaction_types;

SHOW CREATE TABLE inventory_transaction_types;