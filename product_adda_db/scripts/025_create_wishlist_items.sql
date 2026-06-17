/*
===============================================================================
Table       : wishlist_items
Description :
Stores products saved by customers for future purchase.
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

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_wishlist_items_wishlist_item_id
        PRIMARY KEY (pk_wishlist_item_id),

    CONSTRAINT uq_wishlist_product
        UNIQUE (fk_wishlist_id, fk_product_id),

    CONSTRAINT fk_wishlist_items_wishlist_id
        FOREIGN KEY (fk_wishlist_id)
        REFERENCES wishlists(pk_wishlist_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_wishlist_items_product_id
        FOREIGN KEY (fk_product_id)
        REFERENCES products(pk_product_id)
        ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_wishlist_items_wishlist
    ON wishlist_items(fk_wishlist_id);

CREATE INDEX idx_wishlist_items_product
    ON wishlist_items(fk_product_id);

CREATE INDEX idx_wishlist_items_is_active
    ON wishlist_items(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE wishlist_items;

SHOW CREATE TABLE wishlist_items;