/*
===============================================================================
Table       : addresses
Description :
    Stores customer and vendor addresses.

    Supports:
    - Checkout Address Selection
    - Shipping Address
    - Billing Address
    - Vendor Business Address

    Unified design avoids duplicate tables.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS addresses;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE addresses
(
    pk_address_id BINARY(16) NOT NULL,

    fk_user_id BINARY(16) NOT NULL,

    fk_address_type_id BINARY(16) NOT NULL,

    address_line_1 VARCHAR(255) NOT NULL,

    address_line_2 VARCHAR(255) NULL,

    landmark VARCHAR(255) NULL,

    city VARCHAR(100) NOT NULL,

    state VARCHAR(100) NOT NULL,

    postal_code VARCHAR(20) NOT NULL,

    country VARCHAR(100) NOT NULL,

    is_default BOOLEAN NOT NULL DEFAULT FALSE,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_addresses_address_id
        PRIMARY KEY (pk_address_id),

    CONSTRAINT fk_addresses_user_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_addresses_address_type_id
        FOREIGN KEY (fk_address_type_id)
        REFERENCES address_types(pk_address_type_id)
        ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_addresses_user_id
    ON addresses(fk_user_id);

CREATE INDEX idx_addresses_address_type_id
    ON addresses(fk_address_type_id);

CREATE INDEX idx_addresses_city
    ON addresses(city);

CREATE INDEX idx_addresses_state
    ON addresses(state);

CREATE INDEX idx_addresses_postal_code
    ON addresses(postal_code);

CREATE INDEX idx_addresses_is_default
    ON addresses(is_default);

CREATE INDEX idx_addresses_is_active
    ON addresses(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE addresses;

SHOW CREATE TABLE addresses;