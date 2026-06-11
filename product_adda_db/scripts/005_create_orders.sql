/*
===============================================================================
Table       : orders
Description :
    Stores customer orders placed in the marketplace.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS orders
(
    order_id        BIGINT AUTO_INCREMENT NOT NULL,
    customer_id     BIGINT                NOT NULL,

    order_status    VARCHAR(50)           NOT NULL,
    total_amount    DECIMAL(10,2)         NOT NULL,

    CONSTRAINT pk_orders
        PRIMARY KEY (order_id),

    CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_orders_customer_id
    ON orders(customer_id);

CREATE INDEX idx_orders_status
    ON orders(order_status);

CREATE INDEX idx_orders_total_amount
    ON orders(total_amount);

-- ============================================================================
-- Table Verification
-- ============================================================================

DESCRIBE orders;

SHOW CREATE TABLE orders;