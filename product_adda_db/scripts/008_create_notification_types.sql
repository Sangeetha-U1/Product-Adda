/*
===============================================================================
Table : notification_types
Description :
Defines notification event types used by notification services.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS notification_types;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE notification_types
(
    pk_notification_type_id BINARY(16) NOT NULL,

    notification_type_name VARCHAR(100) NOT NULL,

    description VARCHAR(255) NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_notification_types_notification_type_id
        PRIMARY KEY (pk_notification_type_id),

    CONSTRAINT uq_notification_types_name
        UNIQUE (notification_type_name)

);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_notification_types_name
ON notification_types(notification_type_name);

CREATE INDEX idx_notification_types_is_active
ON notification_types(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE notification_types;

SHOW CREATE TABLE notification_types;