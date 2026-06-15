/*
===============================================================================
Table       : products
Description :
    Stores vendor product listings with category mapping, pricing,
    inventory tracking, and status management.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS products
(
    pk_product_id     BINARY(16)        NOT NULL,
    fk_vendor_id      BINARY(16)        NOT NULL,
    fk_category_id    BINARY(16)        NOT NULL,

    title             VARCHAR(255)      NOT NULL,
    description       TEXT              NULL,

    price             DECIMAL(10,2)     NOT NULL,
    discount_price    DECIMAL(10,2)     NULL,

    stock_quantity    INT               NOT NULL DEFAULT 0,

    is_active         BOOLEAN           NOT NULL DEFAULT TRUE,
    
    created_at_utc    TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),

    CONSTRAINT pk_products_product_id
        PRIMARY KEY (pk_product_id),

    CONSTRAINT fk_products_vendor_id
        FOREIGN KEY (fk_vendor_id)
        REFERENCES vendors(pk_vendor_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_products_category_id
        FOREIGN KEY (fk_category_id)
        REFERENCES categories(pk_category_id)
        ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_products_vendor_id
    ON products(fk_vendor_id);

CREATE INDEX idx_products_category_id
    ON products(fk_category_id);

CREATE INDEX idx_products_title
    ON products(title);

CREATE INDEX idx_products_price
    ON products(price);

CREATE INDEX idx_products_is_active
    ON products(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE products;

SHOW CREATE TABLE products;