/*
===============================================================================
Table       : vendor_bank_details
Description :
Stores vendor banking information.

Used for:
- Vendor payout processing
- Settlement transfers
- Revenue disbursement

Stores only the vendor's bank account details and bank verification status.
Individual payout transactions are tracked separately in the
vendor_payouts table.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS vendor_bank_details;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE vendor_bank_details
(
    pk_vendor_bank_detail_id BINARY(16) NOT NULL,

    fk_vendor_id BINARY(16) NOT NULL,

    payout_account_status VARCHAR(20) NOT NULL DEFAULT 'unverified',

    account_holder_name VARCHAR(255) NOT NULL,

    bank_name VARCHAR(255) NOT NULL,

    account_number VARCHAR(50) NOT NULL,

    ifsc_code VARCHAR(20) NOT NULL,

    branch_name VARCHAR(255) NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_vendor_bank_details_id
        PRIMARY KEY (pk_vendor_bank_detail_id),

    CONSTRAINT uq_vendor_bank_details_vendor_id
        UNIQUE (fk_vendor_id),

    CONSTRAINT fk_vendor_bank_details_vendor_id
        FOREIGN KEY (fk_vendor_id)
        REFERENCES vendors(pk_vendor_id)
        ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_vendor_bank_details_vendor_id
    ON vendor_bank_details(fk_vendor_id);

CREATE INDEX idx_vendor_bank_details_ifsc
    ON vendor_bank_details(ifsc_code);

CREATE INDEX idx_vendor_bank_details_is_active
    ON vendor_bank_details(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE vendor_bank_details;

SHOW CREATE TABLE vendor_bank_details;
