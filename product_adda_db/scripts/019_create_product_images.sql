/*
===============================================================================
Table       : product_images
Description :
    Stores product image information.

    Supports:
    - Product Gallery
    - Thumbnail Images
    - Primary Image Selection
    - Binary Storage
    - URL Storage
    - Image Metadata
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS product_images;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE product_images
(
    pk_product_image_id BINARY(16) NOT NULL,

    fk_product_id BINARY(16) NOT NULL,

    image_url VARCHAR(1000) NULL,

    image_data LONGBLOB NULL,

    file_name VARCHAR(255) NULL,

    mime_type VARCHAR(100) NULL,

    file_size_bytes BIGINT NULL,

    width_pixels INT NULL,

    height_pixels INT NULL,

    alt_text VARCHAR(500) NULL,

    is_primary BOOLEAN NOT NULL DEFAULT FALSE,

    display_order INT NOT NULL DEFAULT 0,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_product_images_product_image_id
        PRIMARY KEY (pk_product_image_id),

    CONSTRAINT fk_product_images_product_id
        FOREIGN KEY (fk_product_id)
        REFERENCES products(pk_product_id)
        ON DELETE CASCADE
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_product_images_product_id
    ON product_images(fk_product_id);

CREATE INDEX idx_product_images_is_primary
    ON product_images(is_primary);

CREATE INDEX idx_product_images_display_order
    ON product_images(display_order);

CREATE INDEX idx_product_images_is_active
    ON product_images(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE product_images;

SHOW CREATE TABLE product_images;