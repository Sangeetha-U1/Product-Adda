/*
===============================================================================
Table       : cart_items
Description :
Stores products added into a cart.
Supports add to cart, update quantity, remove product,
and save for later functionality. Tracks historical snapshot pricing at insertion.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS cart_items;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE cart_items
(
    pk_cart_item_id BINARY(16) NOT NULL,
    fk_cart_id BINARY(16) NOT NULL,
    fk_product_id BINARY(16) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price_at_add DECIMAL(10,2) NOT NULL,
    added_at_utc TIMESTAMP NOT NULL,
    is_saved_for_later BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_cart_items_cart_item_id PRIMARY KEY (pk_cart_item_id),
    CONSTRAINT uq_cart_product UNIQUE (fk_cart_id, fk_product_id),
    CONSTRAINT chk_cart_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT fk_cart_items_cart_id FOREIGN KEY (fk_cart_id) REFERENCES carts(pk_cart_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product_id FOREIGN KEY (fk_product_id) REFERENCES products(pk_product_id) ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_cart_items_cart ON cart_items(fk_cart_id);
CREATE INDEX idx_cart_items_product ON cart_items(fk_product_id);
CREATE INDEX idx_cart_items_price_at_add ON cart_items(price_at_add);
CREATE INDEX idx_cart_items_added_at ON cart_items(added_at_utc);
CREATE INDEX idx_cart_items_saved_for_later ON cart_items(is_saved_for_later);
CREATE INDEX idx_cart_items_is_active ON cart_items(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE cart_items;

SHOW CREATE TABLE cart_items;
