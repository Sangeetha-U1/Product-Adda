/*
===============================================================================
Table       : carts
Description :
Stores the customer's active shopping cart.
One customer should have one active cart.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS carts;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE carts
(
    pk_cart_id BINARY(16) NOT NULL,

    fk_user_id BINARY(16) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_carts_cart_id
        PRIMARY KEY (pk_cart_id),

    CONSTRAINT uq_carts_user
        UNIQUE (fk_user_id),

    CONSTRAINT fk_carts_user_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE CASCADE
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_carts_user
    ON carts(fk_user_id);

CREATE INDEX idx_carts_is_active
    ON carts(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE carts;

SHOW CREATE TABLE carts;