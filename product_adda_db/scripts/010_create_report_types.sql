/*
===============================================================================
Table       : report_types
Description :
    Defines supported report categories used by reporting APIs,
    admin dashboard and vendor dashboard.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS report_types;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE report_types
(
    pk_report_type_id BINARY(16) NOT NULL,

    report_type_name VARCHAR(100) NOT NULL,

    description VARCHAR(255) NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_report_types_report_type_id
        PRIMARY KEY (pk_report_type_id),

    CONSTRAINT uq_report_types_name
        UNIQUE (report_type_name)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_report_types_name
    ON report_types(report_type_name);

CREATE INDEX idx_report_types_is_active
    ON report_types(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE report_types;

SHOW CREATE TABLE report_types;