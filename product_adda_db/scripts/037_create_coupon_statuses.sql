/*
===============================================================================
Table       : coupon_statuses
Description :
Stores lookup values representing the lifecycle status of coupons.
Referenced by the coupons table.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS coupon_statuses;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE coupon_statuses
(
    pk_status_id BINARY(16) NOT NULL,

    status_code VARCHAR(50) NOT NULL,

    status_label VARCHAR(100) NOT NULL,

    display_order INT NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_coupon_statuses_status_id
        PRIMARY KEY (pk_status_id),

    CONSTRAINT uq_coupon_statuses_status_code
        UNIQUE (status_code),

    CONSTRAINT uq_coupon_statuses_display_order
        UNIQUE (display_order),

    CONSTRAINT chk_coupon_statuses_display_order_positive
        CHECK (display_order > 0)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_coupon_statuses_status_label
    ON coupon_statuses(status_label);

CREATE INDEX idx_coupon_statuses_is_active
    ON coupon_statuses(is_active);

CREATE INDEX idx_coupon_statuses_created_at
    ON coupon_statuses(created_at_utc);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE coupon_statuses;

SHOW CREATE TABLE coupon_statuses;
