/*
===============================================================================
Table       : delivery_assignment_statuses
Description :
Stores lookup values representing delivery assignment workflow states.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS delivery_assignment_statuses;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE delivery_assignment_statuses
(
    pk_delivery_assignment_status_id BINARY(16) NOT NULL,
    status_name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_delivery_assignment_statuses_id
        PRIMARY KEY (pk_delivery_assignment_status_id),

    CONSTRAINT uq_delivery_assignment_statuses_status_name
        UNIQUE (status_name)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_delivery_assignment_statuses_status_name
ON delivery_assignment_statuses(status_name);

CREATE INDEX idx_delivery_assignment_statuses_is_active
ON delivery_assignment_statuses(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE delivery_assignment_statuses;

SHOW CREATE TABLE delivery_assignment_statuses;