/*
===============================================================================
Table       : invoices
Description :
Stores invoice metadata generated for customer orders.
Represents invoice versioning, generated PDF location, and invoice amount while
the actual PDF document is stored in blob storage.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS invoices;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE invoices
(
    pk_invoice_id BINARY(16) NOT NULL,
    fk_order_id BINARY(16) NOT NULL,
    invoice_number VARCHAR(100) NOT NULL,
    invoice_version INT NOT NULL DEFAULT 1,
    invoice_amount DECIMAL(10,2) NOT NULL,
    file_url VARCHAR(1000) NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_invoices_invoice_id
        PRIMARY KEY (pk_invoice_id),

    CONSTRAINT uq_invoices_invoice_number
        UNIQUE (invoice_number),

    CONSTRAINT uq_invoices_order_id
        UNIQUE (fk_order_id),

    CONSTRAINT fk_invoices_order_id
        FOREIGN KEY (fk_order_id)
        REFERENCES orders(pk_order_id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_invoices_invoice_version
        CHECK (invoice_version > 0),

    CONSTRAINT chk_invoices_invoice_amount
        CHECK (invoice_amount >= 0)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_invoices_invoice_number
    ON invoices(invoice_number);

CREATE INDEX idx_invoices_order
    ON invoices(fk_order_id);

CREATE INDEX idx_invoices_generated_at
    ON invoices(generated_at);

CREATE INDEX idx_invoices_is_active
    ON invoices(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE invoices;

SHOW CREATE TABLE invoices;
