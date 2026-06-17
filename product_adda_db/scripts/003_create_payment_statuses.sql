/*
===============================================================================
Table       : payment_statuses
Description :
Stores payment transaction statuses used by payment processing,
refunds and reporting modules.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS payment_statuses;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE payment_statuses
(
    pk_status_id BINARY(16) NOT NULL,

    status_name VARCHAR(50) NOT NULL,

    description VARCHAR(255) NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_payment_statuses_status_id
        PRIMARY KEY (pk_status_id),

    CONSTRAINT uq_payment_statuses_status_name
        UNIQUE (status_name)

);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_payment_statuses_status_name
ON payment_statuses(status_name);

CREATE INDEX idx_payment_statuses_is_active
ON payment_statuses(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE payment_statuses;

SHOW CREATE TABLE payment_statuses;
