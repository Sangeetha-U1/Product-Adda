/*
===============================================================================
Table       : cart_statuses
Description :
Stores lookup values representing the lifecycle status of a shopping cart.
Referenced by the carts table.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS cart_statuses;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE cart_statuses
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

    CONSTRAINT pk_cart_statuses_status_id
        PRIMARY KEY (pk_status_id),

    CONSTRAINT uq_cart_statuses_status_code
        UNIQUE (status_code),

    CONSTRAINT uq_cart_statuses_display_order
        UNIQUE (display_order),

    CONSTRAINT chk_cart_statuses_display_order_positive
        CHECK (display_order > 0)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_cart_statuses_status_label
    ON cart_statuses(status_label);

CREATE INDEX idx_cart_statuses_is_active
    ON cart_statuses(is_active);

CREATE INDEX idx_cart_statuses_created_at
    ON cart_statuses(created_at_utc);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE cart_statuses;

SHOW CREATE TABLE cart_statuses;
