/*
===============================================================================
Table       : reports
Description :
Stores metadata for generated system reports.
Stores report references and audit information.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

DROP TABLE IF EXISTS reports;


CREATE TABLE reports
(
    pk_report_id BINARY(16) NOT NULL,

    fk_user_id BINARY(16) NOT NULL,

    fk_report_type_id BINARY(16) NOT NULL,

    report_name VARCHAR(255) NOT NULL,

    file_url VARCHAR(1000) NOT NULL,

    generated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,


    CONSTRAINT pk_reports_report_id
        PRIMARY KEY (pk_report_id),


    CONSTRAINT fk_reports_user_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE RESTRICT,


    CONSTRAINT fk_reports_report_type_id
        FOREIGN KEY (fk_report_type_id)
        REFERENCES report_types(pk_report_type_id)
        ON DELETE RESTRICT
);


-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_reports_user_id
ON reports(fk_user_id);


CREATE INDEX idx_reports_report_type_id
ON reports(fk_report_type_id);


CREATE INDEX idx_reports_generated_at
ON reports(generated_at_utc);


CREATE INDEX idx_reports_is_active
ON reports(is_active);


-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE reports;

SHOW CREATE TABLE reports;
