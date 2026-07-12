/*
===============================================================================
Table       : refunds
Description :
Tracks customer refunds for completed payments.

Supports full and partial refunds, payment gateway references, refund
lifecycle tracking, processing status, failure details, and order-triggered
refund events.
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

    fk_order_id BINARY(16) NOT NULL,

    refund_type VARCHAR(20) NOT NULL,

    refund_amount_in_paise BIGINT NOT NULL,

    gateway_refund_id VARCHAR(255) NULL,

    fk_status_id BINARY(16) NOT NULL,

    initiated_by VARCHAR(30) NOT NULL,

    reason VARCHAR(500) NULL,

    refund_triggered_by_order_event VARCHAR(30) NULL,

    processed_at_utc TIMESTAMP NULL,

    failed_at_utc TIMESTAMP NULL,

    failure_reason TEXT NULL,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_refunds_id
        PRIMARY KEY (pk_refund_id),

    CONSTRAINT uq_refunds_gateway_refund_id
        UNIQUE (gateway_refund_id),

    CONSTRAINT fk_refunds_payment_id
        FOREIGN KEY (fk_payment_id)
        REFERENCES payments(pk_payment_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_refunds_order_id
        FOREIGN KEY (fk_order_id)
        REFERENCES orders(pk_order_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_refunds_status_id
        FOREIGN KEY (fk_status_id)
        REFERENCES payment_statuses(pk_status_id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_refunds_amount_positive
        CHECK (refund_amount_in_paise > 0),

    CONSTRAINT chk_refunds_type
        CHECK (refund_type IN ('full', 'partial'))
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_refunds_payment
ON refunds(fk_payment_id);

CREATE INDEX idx_refunds_order
ON refunds(fk_order_id);

CREATE INDEX idx_refunds_status
ON refunds(fk_status_id);

CREATE INDEX idx_refunds_created_at
ON refunds(created_at_utc);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE refunds;

SHOW CREATE TABLE refunds;
