/*
===============================================================================
Table       : wishlist_price_history
Description :
Stores historical price snapshots for wishlist items.
Used for price drop detection and price history tracking.

Seed Data
No seed data required.
This is a transactional table. Do not add it to any seed file.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS wishlist_price_history;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE wishlist_price_history
(
    pk_history_id BINARY(16) NOT NULL,

    fk_wishlist_item_id BINARY(16) NOT NULL,

    price_snapshot DECIMAL(10,2) NOT NULL,

    price_drop_percentage DECIMAL(5,2) DEFAULT NULL,

    snapshot_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_wishlist_price_history_history_id
        PRIMARY KEY (pk_history_id),

    CONSTRAINT fk_wishlist_price_history_wishlist_item_id
        FOREIGN KEY (fk_wishlist_item_id)
        REFERENCES wishlist_items(pk_wishlist_item_id)
        ON DELETE CASCADE,

    CONSTRAINT chk_wishlist_price_history_price_snapshot_positive
        CHECK (price_snapshot >= 0),

    CONSTRAINT chk_wishlist_price_history_price_drop_percentage
        CHECK (price_drop_percentage IS NULL OR
               (price_drop_percentage >= 0 AND price_drop_percentage <= 100))
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_wishlist_price_history_wishlist_item
    ON wishlist_price_history(fk_wishlist_item_id);

CREATE INDEX idx_wishlist_price_history_snapshot_at
    ON wishlist_price_history(snapshot_at_utc);

CREATE INDEX idx_wishlist_price_history_is_active
    ON wishlist_price_history(is_active);

CREATE INDEX idx_wishlist_price_history_created_at
    ON wishlist_price_history(created_at_utc);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE wishlist_price_history;

SHOW CREATE TABLE wishlist_price_history;
