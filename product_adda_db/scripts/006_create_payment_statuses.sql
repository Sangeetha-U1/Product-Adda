/*
===============================================================================
Table       : payment_statuses
Description :
    Stores statuses details for payments.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- 1. Create Lookup Tables First
-- ============================================================================

CREATE TABLE IF NOT EXISTS payment_statuses (
    pk_status_id   BINARY(16)        NOT NULL,
    status_name    VARCHAR(50)       NOT NULL,
    description    VARCHAR(255)      NULL,
    created_at_utc    TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),

    CONSTRAINT pk_payment_statuses_status_id PRIMARY KEY (pk_status_id),
    CONSTRAINT uq_payment_statuses_status_name UNIQUE (status_name)
) ENGINE=InnoDB;

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE payment_statuses;

SHOW CREATE TABLE payment_statuses;