/*
===============================================================================
Table       : order_items
Description :
    Stores individual products within an order.
    Represents many-to-many relationship between orders and products.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS order_items
(
    pk_order_item_id   BINARY(16)        NOT NULL,
    fk_order_id        BINARY(16)        NOT NULL,
    fk_product_id      BINARY(16)        NOT NULL,

    quantity           INT               NOT NULL,
    unit_price         DECIMAL(10,2)     NOT NULL,

    CONSTRAINT pk_order_items_order_item_id
        PRIMARY KEY (pk_order_item_id),

    CONSTRAINT fk_order_items_order_id
        FOREIGN KEY (fk_order_id)
        REFERENCES orders(pk_order_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_order_items_product_id
        FOREIGN KEY (fk_product_id)
        REFERENCES products(pk_product_id)
        ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_order_items_order_id
    ON order_items(fk_order_id);

CREATE INDEX idx_order_items_product_id
    ON order_items(fk_product_id);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE order_items;

SHOW CREATE TABLE order_items;