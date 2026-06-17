/*
===============================================================================
Table       : order_statuses
Description :
Stores order lifecycle statuses used throughout the ProductAdda
order processing workflow.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS order_statuses;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE order_statuses
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

CONSTRAINT pk_order_statuses_status_id
    PRIMARY KEY (pk_status_id),

CONSTRAINT uq_order_statuses_status_name
    UNIQUE (status_name)

);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_order_statuses_status_name
ON order_statuses(status_name);

CREATE INDEX idx_order_statuses_is_active
ON order_statuses(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE order_statuses;

SHOW CREATE TABLE order_statuses;
