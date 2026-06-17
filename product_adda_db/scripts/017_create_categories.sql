/*
===============================================================================
Table       : categories
Description :
    Stores product category hierarchy.

    Supports unlimited category nesting through self-reference.

    Examples:
    Electronics
        └── Mobile Phones

    Electronics
        └── Laptops
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS categories;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE categories
(
    pk_category_id BINARY(16) NOT NULL,

    fk_parent_category_id BINARY(16) NULL,

    category_name VARCHAR(150) NOT NULL,

    category_description TEXT NULL,

    display_order INT NOT NULL DEFAULT 0,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_categories_category_id
        PRIMARY KEY (pk_category_id),

    CONSTRAINT uq_categories_parent_category_id_category_name
        UNIQUE (fk_parent_category_id, category_name),

    CONSTRAINT fk_categories_parent_category_id
        FOREIGN KEY (fk_parent_category_id)
        REFERENCES categories(pk_category_id)
        ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_categories_parent_category_id
    ON categories(fk_parent_category_id);

CREATE INDEX idx_categories_category_name
    ON categories(category_name);

CREATE INDEX idx_categories_display_order
    ON categories(display_order);

CREATE INDEX idx_categories_is_active
    ON categories(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE categories;

SHOW CREATE TABLE categories;