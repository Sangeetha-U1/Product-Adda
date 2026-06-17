/*
===============================================================================
Table       : payment_gateways
Description :
Stores supported payment gateways used by checkout and payment
processing modules.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS payment_gateways;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE payment_gateways
(
    pk_gateway_id BINARY(16) NOT NULL,

    gateway_name VARCHAR(100) NOT NULL,

    description VARCHAR(255) NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_payment_gateways_gateway_id
        PRIMARY KEY (pk_gateway_id),

    CONSTRAINT uq_payment_gateways_gateway_name
        UNIQUE (gateway_name)

);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_payment_gateways_gateway_name
ON payment_gateways(gateway_name);

CREATE INDEX idx_payment_gateways_is_active
ON payment_gateways(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE payment_gateways;

SHOW CREATE TABLE payment_gateways;
