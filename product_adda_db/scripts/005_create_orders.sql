/*
===============================================================================
Table       : orders
Description :
    Stores customer orders placed in the ProductAdda marketplace.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS orders
(
    pk_order_id      BINARY(16)        NOT NULL,
    fk_user_id       BINARY(16)        NOT NULL,

    order_status     VARCHAR(50)       NOT NULL,
    total_amount     DECIMAL(10,2)     NOT NULL,

    CONSTRAINT pk_orders_order_id
        PRIMARY KEY (pk_order_id),

    CONSTRAINT fk_orders_user_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE CASCADE
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_orders_user_id
    ON orders(fk_user_id);

CREATE INDEX idx_orders_status
    ON orders(order_status);

CREATE INDEX idx_orders_total_amount
    ON orders(total_amount);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE orders;

SHOW CREATE TABLE orders;