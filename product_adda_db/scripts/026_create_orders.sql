/*
===============================================================================
Table       : orders
Description :
Stores customer order header information.
Represents a single order placed by a customer.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS orders;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE orders
(
    pk_order_id BINARY(16) NOT NULL,

    fk_user_id BINARY(16) NOT NULL,

    fk_status_id BINARY(16) NOT NULL,

    fk_address_id BINARY(16) NOT NULL,

    total_amount DECIMAL(10,2) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_orders_order_id
        PRIMARY KEY (pk_order_id),

    CONSTRAINT fk_orders_user_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_orders_status_id
        FOREIGN KEY (fk_status_id)
        REFERENCES order_statuses(pk_status_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_orders_address_id
        FOREIGN KEY (fk_address_id)
        REFERENCES addresses(pk_address_id)
        ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_orders_user
    ON orders(fk_user_id);

CREATE INDEX idx_orders_status
    ON orders(fk_status_id);

CREATE INDEX idx_orders_address
    ON orders(fk_address_id);

CREATE INDEX idx_orders_total_amount
    ON orders(total_amount);

CREATE INDEX idx_orders_created_at
    ON orders(created_at_utc);

CREATE INDEX idx_orders_is_active
    ON orders(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE orders;

SHOW CREATE TABLE orders;