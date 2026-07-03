/*
===============================================================================
Table       : carts
Description :
Stores the customer's active or historic shopping carts linked to statuses 
and optional promotional coupons.
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
    fk_cart_status_id BINARY(16) NOT NULL,
    fk_coupon_id BINARY(16) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,
    expires_at_utc TIMESTAMP NULL,

    CONSTRAINT pk_carts_cart_id PRIMARY KEY (pk_cart_id),
    CONSTRAINT uq_carts_user UNIQUE (fk_user_id),
    CONSTRAINT fk_carts_user_id FOREIGN KEY (fk_user_id) REFERENCES users(pk_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_carts_cart_status_id FOREIGN KEY (fk_cart_status_id) REFERENCES cart_statuses(pk_status_id) ON DELETE RESTRICT,
    CONSTRAINT fk_carts_coupon_id FOREIGN KEY (fk_coupon_id) REFERENCES coupons(pk_coupon_id) ON DELETE SET NULL
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_carts_user ON carts(fk_user_id);
CREATE INDEX idx_carts_cart_status ON carts(fk_cart_status_id);
CREATE INDEX idx_carts_coupon ON carts(fk_coupon_id);
CREATE INDEX idx_carts_is_active ON carts(is_active);
CREATE INDEX idx_carts_expires_at ON carts(expires_at_utc);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE carts;

SHOW CREATE TABLE carts;
