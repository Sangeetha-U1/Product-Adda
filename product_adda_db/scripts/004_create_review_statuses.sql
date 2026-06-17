/*
===============================================================================

Table       : review_statuses
Description :
Stores review moderation workflow statuses used by review management
and administration modules.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS review_statuses;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE review_statuses
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

    CONSTRAINT pk_review_statuses_status_id
        PRIMARY KEY (pk_status_id),

    CONSTRAINT uq_review_statuses_status_name
        UNIQUE (status_name)

);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_review_statuses_status_name
ON review_statuses(status_name);

CREATE INDEX idx_review_statuses_is_active
ON review_statuses(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE review_statuses;

SHOW CREATE TABLE review_statuses;
