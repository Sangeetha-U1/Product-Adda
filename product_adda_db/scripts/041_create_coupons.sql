/*
===============================================================================
Table       : coupons
Description :
Stores coupon definitions and validation rules.
Referenced during cart validation and checkout.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS coupons;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE coupons
(
    pk_coupon_id BINARY(16) NOT NULL,

    coupon_code VARCHAR(50) NOT NULL,

    description TEXT NULL,

    fk_discount_type_id BINARY(16) NOT NULL,

    fk_coupon_status_id BINARY(16) NOT NULL,

    discount_value DECIMAL(10,2) NOT NULL,

    maximum_discount_amount DECIMAL(10,2) NULL,

    minimum_purchase_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,

    maximum_global_usage INT NULL,

    maximum_user_usage INT NULL,

    usage_count INT NOT NULL DEFAULT 0,

    is_one_time BOOLEAN NOT NULL DEFAULT FALSE,

    starts_at_utc TIMESTAMP NOT NULL,

    expires_at_utc TIMESTAMP NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_coupons_coupon_id
        PRIMARY KEY (pk_coupon_id),

    CONSTRAINT uq_coupons_coupon_code
        UNIQUE (coupon_code),

    CONSTRAINT fk_coupons_discount_type_id
        FOREIGN KEY (fk_discount_type_id)
        REFERENCES coupon_discount_types(pk_discount_type_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_coupons_coupon_status_id
        FOREIGN KEY (fk_coupon_status_id)
        REFERENCES coupon_statuses(pk_status_id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_coupons_discount_value
        CHECK (discount_value > 0),

    CONSTRAINT chk_coupons_minimum_purchase_amount
        CHECK (minimum_purchase_amount >= 0),

    CONSTRAINT chk_coupons_usage_count
        CHECK (usage_count >= 0),

    CONSTRAINT chk_coupons_maximum_discount_amount
        CHECK (maximum_discount_amount IS NULL OR maximum_discount_amount >= 0),

    CONSTRAINT chk_coupons_maximum_global_usage
        CHECK (maximum_global_usage IS NULL OR maximum_global_usage > 0),

    CONSTRAINT chk_coupons_maximum_user_usage
        CHECK (maximum_user_usage IS NULL OR maximum_user_usage > 0)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_coupons_coupon_status
    ON coupons(fk_coupon_status_id);

CREATE INDEX idx_coupons_discount_type
    ON coupons(fk_discount_type_id);

CREATE INDEX idx_coupons_expires_at
    ON coupons(expires_at_utc);

CREATE INDEX idx_coupons_starts_at
    ON coupons(starts_at_utc);

CREATE INDEX idx_coupons_is_active
    ON coupons(is_active);

CREATE INDEX idx_coupons_created_at
    ON coupons(created_at_utc);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE coupons;

SHOW CREATE TABLE coupons;