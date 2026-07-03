/*
===============================================================================
Table       : coupon_usage_history
Description :
Stores coupon usage history for tracking coupon redemption.
Used to enforce per-user and global coupon usage limits.

Seed Data
No seed data required.
This is a transactional/history table.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS coupon_usage_history;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE coupon_usage_history
(
    pk_coupon_usage_id BINARY(16) NOT NULL,

    fk_coupon_id BINARY(16) NOT NULL,

    fk_user_id BINARY(16) NOT NULL,

    fk_cart_id BINARY(16) NULL,

    fk_order_id BINARY(16) NULL,

    discount_amount DECIMAL(10,2) NOT NULL,

    used_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_coupon_usage_history_coupon_usage_id
        PRIMARY KEY (pk_coupon_usage_id),

    CONSTRAINT fk_coupon_usage_history_coupon_id
        FOREIGN KEY (fk_coupon_id)
        REFERENCES coupons(pk_coupon_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_coupon_usage_history_user_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_coupon_usage_history_cart_id
        FOREIGN KEY (fk_cart_id)
        REFERENCES carts(pk_cart_id)
        ON DELETE SET NULL,

    CONSTRAINT fk_coupon_usage_history_order_id
        FOREIGN KEY (fk_order_id)
        REFERENCES orders(pk_order_id)
        ON DELETE SET NULL,

    CONSTRAINT chk_coupon_usage_history_discount_amount
        CHECK (discount_amount >= 0)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_coupon_usage_history_coupon
    ON coupon_usage_history(fk_coupon_id);

CREATE INDEX idx_coupon_usage_history_user
    ON coupon_usage_history(fk_user_id);

CREATE INDEX idx_coupon_usage_history_cart
    ON coupon_usage_history(fk_cart_id);

CREATE INDEX idx_coupon_usage_history_order
    ON coupon_usage_history(fk_order_id);

CREATE INDEX idx_coupon_usage_history_used_at
    ON coupon_usage_history(used_at_utc);

CREATE INDEX idx_coupon_usage_history_is_active
    ON coupon_usage_history(is_active);

CREATE INDEX idx_coupon_usage_history_created_at
    ON coupon_usage_history(created_at_utc);

CREATE INDEX idx_coupon_usage_history_coupon_user
    ON coupon_usage_history(fk_coupon_id, fk_user_id);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE coupon_usage_history;

SHOW CREATE TABLE coupon_usage_history;
