/*
===============================================================================
Table       : wishlists
Description :
Stores customer wishlist container.
One customer should have one wishlist.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS wishlists;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE wishlists
(
    pk_wishlist_id BINARY(16) NOT NULL,

    fk_user_id BINARY(16) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_wishlists_wishlist_id
        PRIMARY KEY (pk_wishlist_id),

    CONSTRAINT uq_wishlists_user
        UNIQUE (fk_user_id),

    CONSTRAINT fk_wishlists_user_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE CASCADE
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_wishlists_user
    ON wishlists(fk_user_id);

CREATE INDEX idx_wishlists_is_active
    ON wishlists(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE wishlists;

SHOW CREATE TABLE wishlists;