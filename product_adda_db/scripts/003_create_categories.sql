/*
===============================================================================
Table       : categories
Description :
    Stores product categories for marketplace products.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS categories
(
    pk_category_id    BINARY(16)      NOT NULL,
    category_name     VARCHAR(150)    NOT NULL,
    
    created_at_utc    TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),

    CONSTRAINT pk_categories_category_id
        PRIMARY KEY (pk_category_id),

    CONSTRAINT uq_categories_category_name
        UNIQUE (category_name)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_categories_category_name
    ON categories(category_name);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE categories;

SHOW CREATE TABLE categories;