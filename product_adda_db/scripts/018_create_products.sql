/*
===============================================================================
Table       : products
Description :
    Stores vendor product catalog information.

    Core marketplace table.

    Supports:
    - Product Listing
    - Product Search
    - Product Filters
    - Product Detail Page
    - Cart
    - Wishlist
    - Orders
    - Inventory

    Adds `status` VARCHAR column to the `products` table to track product
    lifecycle state. References `product_status_lookup.status_code`.

    Stored as plain VARCHAR string (Zero Enum Rule).
    Default value set to 'APPROVED' for all pre-existing seed data products.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS products;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE products
(
    pk_product_id BINARY(16) NOT NULL,

    fk_vendor_id BINARY(16) NOT NULL,

    fk_category_id BINARY(16) NOT NULL,

    fk_brand_id BINARY(16) NOT NULL,

    title VARCHAR(255) NOT NULL,

    description TEXT NULL,

    sku VARCHAR(100) NOT NULL,

    price DECIMAL(10,2) NOT NULL,

    discount_price DECIMAL(10,2) NULL,

    stock_quantity INT NOT NULL DEFAULT 0,

    average_rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,

    total_reviews INT NOT NULL DEFAULT 0,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    fk_status_id BINARY(16) NOT NULL;

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_products_product_id
        PRIMARY KEY (pk_product_id),

    CONSTRAINT uq_products_vendor_id_sku
        UNIQUE (fk_vendor_id, sku),

    CONSTRAINT fk_products_vendor_id
        FOREIGN KEY (fk_vendor_id)
        REFERENCES vendors(pk_vendor_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_products_category_id
        FOREIGN KEY (fk_category_id)
        REFERENCES categories(pk_category_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_products_brand_id
        FOREIGN KEY (fk_brand_id)
        REFERENCES brands(pk_brand_id)
        ON DELETE RESTRICT
    
    CONSTRAINT fk_products_status_lookup
        FOREIGN KEY (fk_status_id)
        REFERENCES product_status_lookup(pk_status_id)
        ON DELETE RESTRICT;

);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_products_vendor_id
    ON products(fk_vendor_id);

CREATE INDEX idx_products_category_id
    ON products(fk_category_id);

CREATE INDEX idx_products_brand_id
    ON products(fk_brand_id);

CREATE INDEX idx_products_title
    ON products(title);

CREATE INDEX idx_products_sku
    ON products(sku);

CREATE INDEX idx_products_price
    ON products(price);

CREATE INDEX idx_products_average_rating
    ON products(average_rating);

CREATE INDEX idx_products_is_active
    ON products(is_active);

CREATE INDEX idx_products_fk_status_id
    ON products(fk_status_id);
    
-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE products;

SHOW CREATE TABLE products;