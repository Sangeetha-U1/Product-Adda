/*
===============================================================================
Table       : inventory_transactions
Description :
    Stores inventory movement history.

    Provides complete audit trail for stock changes.

    Supports:
    - Stock In
    - Stock Out
    - Return
    - Adjustment

    Every inventory movement must be recorded.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS inventory_transactions;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE inventory_transactions
(
    pk_inventory_transaction_id BINARY(16) NOT NULL,

    fk_product_id BINARY(16) NOT NULL,

    fk_transaction_type_id BINARY(16) NOT NULL,

    quantity INT NOT NULL,

    reference_type VARCHAR(100) NULL,

    reference_id BINARY(16) NULL,

    remarks VARCHAR(500) NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_inventory_transactions_quantity_positive
    CHECK (quantity > 0),

    CONSTRAINT pk_inventory_transactions_inventory_transaction_id
        PRIMARY KEY (pk_inventory_transaction_id),

    CONSTRAINT fk_inventory_transactions_product_id
        FOREIGN KEY (fk_product_id)
        REFERENCES products(pk_product_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_inventory_transactions_transaction_type_id
        FOREIGN KEY (fk_transaction_type_id)
        REFERENCES inventory_transaction_types(pk_transaction_type_id)
        ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_inventory_transactions_product
    ON inventory_transactions(fk_product_id);

CREATE INDEX idx_inventory_transactions_type
    ON inventory_transactions(fk_transaction_type_id);

CREATE INDEX idx_inventory_transactions_reference_type
    ON inventory_transactions(reference_type);

CREATE INDEX idx_inventory_transactions_reference_id
    ON inventory_transactions(reference_id);

CREATE INDEX idx_inventory_transactions_created_at
    ON inventory_transactions(created_at_utc);

CREATE INDEX idx_inventory_transactions_is_active
    ON inventory_transactions(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE inventory_transactions;

SHOW CREATE TABLE inventory_transactions;