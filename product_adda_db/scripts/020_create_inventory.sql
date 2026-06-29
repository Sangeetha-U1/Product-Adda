/*
===============================================================================
Table       : inventory
Description :
    Stores current inventory state for each product.

    Provides:
    - Available Stock
    - Reserved Stock
    - Low Stock Monitoring
    - Inventory Dashboard

    One product should have exactly one inventory record.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS inventory;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE inventory
(
    pk_inventory_id BINARY(16) NOT NULL,

    fk_product_id BINARY(16) NOT NULL,

    available_quantity INT NOT NULL DEFAULT 0,

    reserved_quantity INT NOT NULL DEFAULT 0,

    low_stock_threshold INT NOT NULL DEFAULT 10,

    max_stock INT NOT NULL DEFAULT 1000,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_inventory_inventory_id
        PRIMARY KEY (pk_inventory_id),

    CONSTRAINT uq_inventory_product
        UNIQUE (fk_product_id),

    CONSTRAINT fk_inventory_product_id
        FOREIGN KEY (fk_product_id)
        REFERENCES products(pk_product_id)
        ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_inventory_product
    ON inventory(fk_product_id);

CREATE INDEX idx_inventory_available_quantity
    ON inventory(available_quantity);

CREATE INDEX idx_inventory_low_stock_threshold
    ON inventory(low_stock_threshold);

CREATE INDEX idx_inventory_is_active
    ON inventory(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE inventory;

SHOW CREATE TABLE inventory;