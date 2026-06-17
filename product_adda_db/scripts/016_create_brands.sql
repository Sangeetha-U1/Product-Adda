/*
===============================================================================
Table       : brands
Description :
    Stores product brands.

    Required for:
    - Product Search
    - Brand Filters
    - Vendor Product Management

    Normalizing brands avoids duplication and improves filtering.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS brands;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE brands
(
    pk_brand_id BINARY(16) NOT NULL,

    brand_name VARCHAR(150) NOT NULL,

    brand_description TEXT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_brands_brand_id
        PRIMARY KEY (pk_brand_id),

    CONSTRAINT uq_brands_brand_name
        UNIQUE (brand_name)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_brands_brand_name
    ON brands(brand_name);

CREATE INDEX idx_brands_is_active
    ON brands(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE brands;

SHOW CREATE TABLE brands;