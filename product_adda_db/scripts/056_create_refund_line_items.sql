/*
===============================================================================
Table       : refund_line_items
Description :
Tracks the item-level breakdown of refunded order items within a refund
transaction.

Supports partial refunds by associating individual order items with their
refunded amounts.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS refund_line_items;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE refund_line_items
(
    pk_refund_line_item_id BINARY(16) NOT NULL,

    fk_refund_id BINARY(16) NOT NULL,

    fk_order_item_id BINARY(16) NOT NULL,

    refund_amount_in_paise BIGINT NOT NULL,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    CONSTRAINT pk_refund_line_items_id
        PRIMARY KEY (pk_refund_line_item_id),

    CONSTRAINT fk_refund_line_items_refund_id
        FOREIGN KEY (fk_refund_id)
        REFERENCES refunds(pk_refund_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_refund_line_items_order_item_id
        FOREIGN KEY (fk_order_item_id)
        REFERENCES order_items(pk_order_item_id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_refund_line_items_amount_positive
        CHECK (refund_amount_in_paise > 0)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_refund_line_items_refund
ON refund_line_items(fk_refund_id);

CREATE INDEX idx_refund_line_items_order_item
ON refund_line_items(fk_order_item_id);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE refund_line_items;

SHOW CREATE TABLE refund_line_items;
