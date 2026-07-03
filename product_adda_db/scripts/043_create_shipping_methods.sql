/*
===============================================================================
Table       : shipping_methods
Description :
Stores available shipping methods and their pricing configuration.
Referenced during checkout and order creation.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS shipping_methods;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE shipping_methods
(
    pk_shipping_method_id BINARY(16) NOT NULL,

    shipping_method_name VARCHAR(100) NOT NULL,

    description TEXT NULL,

    base_cost DECIMAL(10,2) NOT NULL,

    cost_per_kg DECIMAL(10,2) NOT NULL DEFAULT 0.00,

    estimated_delivery_days INT NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_shipping_methods_shipping_method_id
        PRIMARY KEY (pk_shipping_method_id),

    CONSTRAINT uq_shipping_methods_shipping_method_name
        UNIQUE (shipping_method_name),

    CONSTRAINT chk_shipping_methods_base_cost
        CHECK (base_cost >= 0),

    CONSTRAINT chk_shipping_methods_cost_per_kg
        CHECK (cost_per_kg >= 0),

    CONSTRAINT chk_shipping_methods_estimated_delivery_days
        CHECK (estimated_delivery_days > 0)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_shipping_methods_name
    ON shipping_methods(shipping_method_name);

CREATE INDEX idx_shipping_methods_is_active
    ON shipping_methods(is_active);

CREATE INDEX idx_shipping_methods_created_at
    ON shipping_methods(created_at_utc);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE shipping_methods;

SHOW CREATE TABLE shipping_methods;
