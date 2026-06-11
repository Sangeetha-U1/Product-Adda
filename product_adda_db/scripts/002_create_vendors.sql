/*
===============================================================================
Table       : vendors
Description :
    Stores vendor business information linked to users.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS vendors
(
    vendor_id      BIGINT AUTO_INCREMENT NOT NULL,
    user_id        BIGINT                NOT NULL,

    business_name  VARCHAR(255)          NOT NULL,
    gst_number     VARCHAR(50)           NOT NULL,

    CONSTRAINT pk_vendors
        PRIMARY KEY (vendor_id),

    CONSTRAINT uq_vendors_gst
        UNIQUE (gst_number),

    CONSTRAINT fk_vendors_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_vendors_user_id
    ON vendors(user_id);

CREATE INDEX idx_vendors_business_name
    ON vendors(business_name);

-- ============================================================================
-- Table Verification
-- ============================================================================

DESCRIBE vendors;

SHOW CREATE TABLE vendors;