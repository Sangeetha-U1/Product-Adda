/*
===============================================================================
Table       : refund_line_items
Description :
Tracks the granular breakdown of individual order items being refunded within 
a specific refund transaction request.
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
    quantity INT NOT NULL,
    refund_amount_in_paise BIGINT NOT NULL,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_refund_line_items_id PRIMARY KEY (pk_refund_line_item_id),
    CONSTRAINT chk_refund_line_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_refund_line_items_amount CHECK (refund_amount_in_paise >= 0),
    
    CONSTRAINT fk_refund_line_items_refund_id FOREIGN KEY (fk_refund_id) REFERENCES refunds(pk_refund_id) ON DELETE CASCADE,
    CONSTRAINT fk_refund_line_items_order_item_id FOREIGN KEY (fk_order_item_id) REFERENCES order_items(pk_order_item_id) ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_refund_line_items_refund ON refund_line_items(fk_refund_id);
CREATE INDEX idx_refund_line_items_order_item ON refund_line_items(fk_order_item_id);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE refund_line_items;

SHOW CREATE TABLE refund_line_items;
