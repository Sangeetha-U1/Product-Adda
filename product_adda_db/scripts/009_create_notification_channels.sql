/*
===============================================================================
Table       : notification_channels
Description :
    Defines notification delivery channels used by email, SMS and
    push notification services.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS notification_channels;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE notification_channels
(
    pk_channel_id BINARY(16) NOT NULL,

    channel_name VARCHAR(50) NOT NULL,

    description VARCHAR(255) NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_notification_channels_channel_id
        PRIMARY KEY (pk_channel_id),

    CONSTRAINT uq_notification_channels_name
        UNIQUE (channel_name)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_notification_channels_name
    ON notification_channels(channel_name);

CREATE INDEX idx_notification_channels_is_active
    ON notification_channels(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE notification_channels;

SHOW CREATE TABLE notification_channels;