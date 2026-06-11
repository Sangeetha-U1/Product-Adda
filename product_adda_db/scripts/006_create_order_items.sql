/*
===============================================================================
Table       : order_items
Description :
    Stores individual products inside an order.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS order_items
(
    item_id     BIGINT AUTO_INCREMENT NOT NULL,
    order_id    BIGINT                NOT NULL,
    product_id  BIGINT                NOT NULL,
    quantity    INT                   NOT NULL,

    CONSTRAINT pk_order_items
        PRIMARY KEY (item_id),

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(order_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(product_id)
        ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_order_items_order_id
    ON order_items(order_id);

CREATE INDEX idx_order_items_product_id
    ON order_items(product_id);

-- ============================================================================
-- Table Verification
-- ============================================================================

DESCRIBE order_items;

SHOW CREATE TABLE order_items;