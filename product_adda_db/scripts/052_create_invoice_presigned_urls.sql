/*
===============================================================================
Table       : invoice_presigned_urls
Description :
Stores presigned URLs for downloading invoices along with expiration metadata.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS invoice_presigned_urls;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE invoice_presigned_urls
(
    pk_presigned_url_id BINARY(16) NOT NULL,
    fk_invoice_id BINARY(16) NOT NULL,
    presigned_url TEXT NOT NULL,
    expires_at_utc TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_invoice_presigned_urls_id
        PRIMARY KEY (pk_presigned_url_id),

    CONSTRAINT fk_invoice_presigned_urls_invoice_id
        FOREIGN KEY (fk_invoice_id) REFERENCES invoices (pk_invoice_id) 
        ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_invoice_presigned_lookup
ON invoice_presigned_urls(fk_invoice_id, is_active);

CREATE INDEX idx_invoice_presigned_expires_at
ON invoice_presigned_urls(expires_at_utc);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE invoice_presigned_urls;

SHOW CREATE TABLE invoice_presigned_urls;
