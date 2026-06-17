/*
===============================================================================
Table       : vendors
Description :
    Stores vendor business profile information.

    Represents sellers on the marketplace.

    Supports:
    - Vendor Registration
    - Store Management
    - Vendor Dashboard
    - Revenue Tracking
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS vendors;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE vendors
(
    pk_vendor_id BINARY(16) NOT NULL,

    fk_user_id BINARY(16) NOT NULL,

    business_name VARCHAR(255) NOT NULL,

    store_name VARCHAR(255) NOT NULL,

    gst_number VARCHAR(50) NOT NULL,

    business_description TEXT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_vendors_vendor_id
        PRIMARY KEY (pk_vendor_id),

    CONSTRAINT uq_vendors_gst_number
        UNIQUE (gst_number),

    CONSTRAINT uq_vendors_user_id
        UNIQUE (fk_user_id),

    CONSTRAINT fk_vendors_user_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_vendors_user_id
    ON vendors(fk_user_id);

CREATE INDEX idx_vendors_business_name
    ON vendors(business_name);

CREATE INDEX idx_vendors_store_name
    ON vendors(store_name);

CREATE INDEX idx_vendors_is_active
    ON vendors(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE vendors;

SHOW CREATE TABLE vendors;