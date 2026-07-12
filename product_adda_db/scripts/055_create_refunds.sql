/*
===============================================================================
Table       : refunds
Description :
Tracks customer refund requests, amounts processed in paise, gateway handling 
references, and the completion status.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS refunds;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE refunds
(
    pk_refund_id BINARY(16) NOT NULL,
    fk_payment_id BINARY(16) NOT NULL,
    fk_status_id BINARY(16) NOT NULL,
    refund_amount_in_paise BIGINT NOT NULL,
    reason VARCHAR(500) NULL,
    gateway_refund_id VARCHAR(255) NULL,
    error_message TEXT NULL,
    refunded_at_utc TIMESTAMP NULL DEFAULT NULL,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_refunds_id PRIMARY KEY (pk_refund_id),
    CONSTRAINT chk_refunds_amount_positive CHECK (refund_amount_in_paise > 0),
    
    CONSTRAINT fk_refunds_payment_id FOREIGN KEY (fk_payment_id) REFERENCES payments(pk_payment_id) ON DELETE RESTRICT,
    CONSTRAINT fk_refunds_status_id FOREIGN KEY (fk_status_id) REFERENCES payment_statuses(pk_status_id) ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_refunds_payment ON refunds(fk_payment_id);
CREATE INDEX idx_refunds_status ON refunds(fk_status_id);
CREATE INDEX idx_refunds_gateway ON refunds(gateway_refund_id);
CREATE INDEX idx_refunds_created_at ON refunds(created_at_utc);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE refunds;

SHOW CREATE TABLE refunds;
