/*
===============================================================================
Table       : wishlist_items
Description :
Stores products saved by customers for future purchase. Tracks historic pricing snapshot, soft-deletes, and link expiration.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS wishlist_items;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE wishlist_items
(
    pk_wishlist_item_id BINARY(16) NOT NULL,
    fk_wishlist_id BINARY(16) NOT NULL,
    fk_product_id BINARY(16) NOT NULL,
    price_at_add DECIMAL(10,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,
    expires_at_utc TIMESTAMP NULL,

    CONSTRAINT pk_wishlist_items_wishlist_item_id PRIMARY KEY (pk_wishlist_item_id),
    CONSTRAINT fk_wishlist_items_wishlist_id FOREIGN KEY (fk_wishlist_id) REFERENCES wishlists(pk_wishlist_id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_items_product_id FOREIGN KEY (fk_product_id) REFERENCES products(pk_product_id) ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_wishlist_items_wishlist ON wishlist_items(fk_wishlist_id);
CREATE INDEX idx_wishlist_items_product ON wishlist_items(fk_product_id);
CREATE INDEX idx_wishlist_items_price_at_add ON wishlist_items(price_at_add);
CREATE INDEX idx_wishlist_items_expires_at ON wishlist_items(expires_at_utc);
CREATE INDEX idx_wishlist_items_is_active ON wishlist_items(is_active);
CREATE INDEX idx_wishlist_items_is_deleted ON wishlist_items(is_deleted);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE wishlist_items;

SHOW CREATE TABLE wishlist_items;