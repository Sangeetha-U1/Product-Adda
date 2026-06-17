/*
===============================================================================
Table       : order_items
Description :
Stores products belonging to an order.
Maintains product snapshot information even if product changes later.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS order_items;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE order_items
(
    pk_order_item_id BINARY(16) NOT NULL,

    fk_order_id BINARY(16) NOT NULL,

    fk_product_id BINARY(16) NOT NULL,

    product_name_snapshot VARCHAR(255) NOT NULL,

    quantity INT NOT NULL,

    unit_price DECIMAL(10,2) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_order_items_quantity_positive
        CHECK (quantity > 0),

    CONSTRAINT chk_order_items_unit_price_positive
        CHECK (unit_price >= 0),

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

CREATE INDEX idx_order_items_order
    ON order_items(fk_order_id);

CREATE INDEX idx_order_items_product
    ON order_items(fk_product_id);

CREATE INDEX idx_order_items_created_at
    ON order_items(created_at_utc);

CREATE INDEX idx_order_items_is_active
    ON order_items(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE order_items;

SHOW CREATE TABLE order_items;