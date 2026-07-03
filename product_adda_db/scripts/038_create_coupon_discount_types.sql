/*
===============================================================================
Table       : coupon_discount_types
Description :
Stores lookup values representing coupon discount calculation types.
Referenced by the coupons table.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS coupon_discount_types;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE coupon_discount_types
(
    pk_discount_type_id BINARY(16) NOT NULL,

    discount_type_code VARCHAR(50) NOT NULL,

    discount_type_label VARCHAR(100) NOT NULL,

    display_order INT NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_coupon_discount_types_discount_type_id
        PRIMARY KEY (pk_discount_type_id),

    CONSTRAINT uq_coupon_discount_types_code
        UNIQUE (discount_type_code),

    CONSTRAINT uq_coupon_discount_types_display_order
        UNIQUE (display_order),

    CONSTRAINT chk_coupon_discount_types_display_order_positive
        CHECK (display_order > 0)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_coupon_discount_types_label
    ON coupon_discount_types(discount_type_label);

CREATE INDEX idx_coupon_discount_types_is_active
    ON coupon_discount_types(is_active);

CREATE INDEX idx_coupon_discount_types_created_at
    ON coupon_discount_types(created_at_utc);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE coupon_discount_types;

SHOW CREATE TABLE coupon_discount_types;
