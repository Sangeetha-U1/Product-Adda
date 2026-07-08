/*
===============================================================================
Table       : invoice_line_items
Description :
Stores individual invoice item details generated from order items.
Represents product-level billing information including quantity, pricing,
and calculated line amounts for each invoice.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS invoice_line_items;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE invoice_line_items
(
    pk_invoice_line_item_id BINARY(16) NOT NULL,
    fk_invoice_id BINARY(16) NOT NULL,
    fk_order_item_id BINARY(16) NOT NULL,
    item_description VARCHAR(500) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    line_amount DECIMAL(10,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_invoice_line_items_invoice_line_item_id
        PRIMARY KEY (pk_invoice_line_item_id),

    CONSTRAINT fk_invoice_line_items_invoice_id
        FOREIGN KEY (fk_invoice_id)
        REFERENCES invoices(pk_invoice_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_invoice_line_items_order_item_id
        FOREIGN KEY (fk_order_item_id)
        REFERENCES order_items(pk_order_item_id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_invoice_line_items_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_invoice_line_items_unit_price
        CHECK (unit_price >= 0),

    CONSTRAINT chk_invoice_line_items_line_amount
        CHECK (line_amount >= 0)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_invoice_line_items_invoice
    ON invoice_line_items(fk_invoice_id);

CREATE INDEX idx_invoice_line_items_order_item
    ON invoice_line_items(fk_order_item_id);

CREATE INDEX idx_invoice_line_items_line_amount
    ON invoice_line_items(line_amount);

CREATE INDEX idx_invoice_line_items_is_active
    ON invoice_line_items(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE invoice_line_items;

SHOW CREATE TABLE invoice_line_items;