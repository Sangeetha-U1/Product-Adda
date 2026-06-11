/*
===============================================================================
Table       : products
Description :
    Stores products listed by vendors under categories.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS products
(
    product_id       BIGINT AUTO_INCREMENT NOT NULL,
    vendor_id        BIGINT                NOT NULL,
    category_id      BIGINT                NOT NULL,

    title            VARCHAR(255)          NOT NULL,
    description      TEXT                  NULL,
    price            DECIMAL(10,2)         NOT NULL,
    stock_quantity   INT                   NOT NULL,

    payment_id       BIGINT                NULL,
    order_id         BIGINT                NULL,

    CONSTRAINT pk_products
        PRIMARY KEY (product_id),

    CONSTRAINT fk_products_vendor
        FOREIGN KEY (vendor_id)
        REFERENCES vendors(vendor_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES categories(category_id)
        ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_products_vendor_id
    ON products(vendor_id);

CREATE INDEX idx_products_category_id
    ON products(category_id);

CREATE INDEX idx_products_title
    ON products(title);

CREATE INDEX idx_products_price
    ON products(price);

-- ============================================================================
-- Table Verification
-- ============================================================================

DESCRIBE products;

SHOW CREATE TABLE products;