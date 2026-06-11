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
    category_id     BIGINT AUTO_INCREMENT NOT NULL,
    category_name   VARCHAR(150)          NOT NULL,

    CONSTRAINT pk_categories
        PRIMARY KEY (category_id),

    CONSTRAINT uq_categories_name
        UNIQUE (category_name)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_categories_name
    ON categories(category_name);

-- ============================================================================
-- Table Verification
-- ============================================================================

DESCRIBE categories;

SHOW CREATE TABLE categories;