/*
===============================================================================
Table       : delivery_partner_statuses
Description :
Stores lookup values representing delivery partner availability statuses.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS delivery_partner_statuses;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE delivery_partner_statuses
(
    pk_delivery_partner_status_id BINARY(16) NOT NULL,
    status_name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_delivery_partner_statuses_id
        PRIMARY KEY (pk_delivery_partner_status_id),

    CONSTRAINT uq_delivery_partner_statuses_status_name
        UNIQUE (status_name)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_delivery_partner_statuses_status_name
ON delivery_partner_statuses(status_name);

CREATE INDEX idx_delivery_partner_statuses_is_active
ON delivery_partner_statuses(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE delivery_partner_statuses;

SHOW CREATE TABLE delivery_partner_statuses;

