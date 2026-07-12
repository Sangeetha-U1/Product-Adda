/*
===============================================================================
Table       : payments
Description :
Stores payment transaction details for customer orders.
Maintains payment history, payment gateway references, customer payment source
references, idempotency support, webhook metadata, failure tracking,
and transaction audit information.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS payments;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE payments
(
    pk_payment_id BINARY(16) NOT NULL,
    fk_order_id BINARY(16) NOT NULL,
    fk_user_id BINARY(16) NOT NULL,
    idempotency_key BINARY(16) NOT NULL,
    fk_status_id BINARY(16) NOT NULL,
    fk_gateway_id BINARY(16) NOT NULL,

    payment_method VARCHAR(50) NOT NULL,
    fk_payment_source_id BINARY(16) NULL,

    currency VARCHAR(3) NOT NULL DEFAULT 'INR',

    gateway_transaction_id VARCHAR(255) NULL,
    gateway_order_id VARCHAR(255) NULL,
    gateway_payment_link_id VARCHAR(255) NULL,
    gateway_signature VARCHAR(500) NULL,

    error_message TEXT NULL,

    amount_in_paise BIGINT NOT NULL,

    paid_at_utc TIMESTAMP NULL,
    captured_at_utc TIMESTAMP NULL,
    failed_at_utc TIMESTAMP NULL,

    failure_reason TEXT NULL,
    webhook_received_at_utc TIMESTAMP NULL,

    metadata JSON NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_payments_payment_id PRIMARY KEY (pk_payment_id),

    CONSTRAINT uq_payments_order UNIQUE (fk_order_id),
    CONSTRAINT uq_payments_idempotency_key UNIQUE (idempotency_key),

    CONSTRAINT chk_payments_amount_positive
        CHECK (amount_in_paise >= 0),

    CONSTRAINT fk_payments_order_id
        FOREIGN KEY (fk_order_id)
        REFERENCES orders(pk_order_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_payments_customer_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_payments_status_id
        FOREIGN KEY (fk_status_id)
        REFERENCES payment_statuses(pk_status_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_payments_gateway_id
        FOREIGN KEY (fk_gateway_id)
        REFERENCES payment_gateways(pk_gateway_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_payments_payment_source_id
        FOREIGN KEY (fk_payment_source_id)
        REFERENCES payment_methods(pk_payment_method_id)
        ON DELETE SET NULL
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_payments_order
ON payments(fk_order_id);

CREATE INDEX idx_payments_customer
ON payments(fk_user_id);

CREATE INDEX idx_payments_status
ON payments(fk_status_id);

CREATE INDEX idx_payments_gateway
ON payments(fk_gateway_id);

CREATE INDEX idx_payments_method
ON payments(payment_method);

CREATE INDEX idx_payments_gateway_transaction
ON payments(gateway_transaction_id);

CREATE INDEX idx_payments_paid_at
ON payments(paid_at_utc);

CREATE INDEX idx_payments_created_at
ON payments(created_at_utc);

CREATE INDEX idx_payments_is_active
ON payments(is_active);

CREATE INDEX idx_payments_captured_at
ON payments(captured_at_utc);

CREATE INDEX idx_payments_payment_source
ON payments(fk_payment_source_id);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE payments;

SHOW CREATE TABLE payments;
