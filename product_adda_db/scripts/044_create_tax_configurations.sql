/*
===============================================================================
Table       : tax_configurations
Description :
Stores configurable tax rates applicable for different regions.
Referenced during checkout for tax calculation.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS tax_configurations;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE tax_configurations
(
    pk_tax_configuration_id BINARY(16) NOT NULL,

    region_name VARCHAR(100) NOT NULL,

    tax_percentage DECIMAL(5,2) NOT NULL,

    effective_from DATE NOT NULL,

    effective_to DATE NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_tax_configurations_tax_configuration_id
        PRIMARY KEY (pk_tax_configuration_id),

    CONSTRAINT uq_tax_configurations_region_effective_from
        UNIQUE (region_name, effective_from),

    CONSTRAINT chk_tax_configurations_tax_percentage
        CHECK (tax_percentage >= 0 AND tax_percentage <= 100)

    CONSTRAINT chk_tax_configurations_effective_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        );
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_tax_configurations_region
    ON tax_configurations(region_name);

CREATE INDEX idx_tax_configurations_effective_from
    ON tax_configurations(effective_from);

CREATE INDEX idx_tax_configurations_effective_to
    ON tax_configurations(effective_to);

CREATE INDEX idx_tax_configurations_is_active
    ON tax_configurations(is_active);

CREATE INDEX idx_tax_configurations_created_at
    ON tax_configurations(created_at_utc);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE tax_configurations;

SHOW CREATE TABLE tax_configurations;
