/*
===============================================================================
Table       : vendors
Description :
    Stores vendor business details linked to users table.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS vendors
(
    pk_vendor_id    BINARY(16)      NOT NULL,
    fk_user_id      BINARY(16)      NOT NULL,

    business_name   VARCHAR(255)    NOT NULL,
    gst_number      VARCHAR(50)     NOT NULL,

    CONSTRAINT pk_vendors_vendor_id
        PRIMARY KEY (pk_vendor_id),

    CONSTRAINT uq_vendors_gst_number
        UNIQUE (gst_number),

    CONSTRAINT fk_vendors_user_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE CASCADE
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_vendors_fk_user_id
    ON vendors(fk_user_id);

CREATE INDEX idx_vendors_business_name
    ON vendors(business_name);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE vendors;

SHOW CREATE TABLE vendors;