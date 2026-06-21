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

    fk_status_id    BINARY(16)        NOT NULL,
    total_amount     DECIMAL(10,2)     NOT NULL,
    
	created_at_utc    TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    
    CONSTRAINT pk_orders_order_id
        PRIMARY KEY (pk_order_id),

    CONSTRAINT fk_orders_user_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE CASCADE,
	CONSTRAINT fk_orders_status_id FOREIGN KEY (fk_status_id) REFERENCES order_statuses(pk_status_id)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_orders_user_id
    ON orders(fk_user_id);

CREATE INDEX idx_orders_status_id ON orders(fk_status_id);

CREATE INDEX idx_orders_total_amount
    ON orders(total_amount);

CREATE INDEX idx_orders_created_at_utc
    ON orders(created_at_utc);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE orders;

SHOW CREATE TABLE orders;