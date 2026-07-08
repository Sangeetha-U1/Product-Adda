/*
===============================================================================
Table       : item_statuses
Description :
Stores lookup values representing the lifecycle status of individual order
items. This table is referenced by order_items and is independent of the
overall order_statuses table.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS item_statuses;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE item_statuses
(
    pk_item_status_id BINARY(16) NOT NULL,
    status_name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_item_statuses_item_status_id
        PRIMARY KEY (pk_item_status_id),

    CONSTRAINT uq_item_statuses_status_name
        UNIQUE (status_name)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_item_statuses_status_name
ON item_statuses(status_name);

CREATE INDEX idx_item_statuses_is_active
ON item_statuses(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE item_statuses;

SHOW CREATE TABLE item_statuses;
