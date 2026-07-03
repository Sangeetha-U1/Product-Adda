/*
===============================================================================
Table       : payments
Description :
Stores payment transaction details for customer orders.
Maintains payment history, gateway tracking configurations, error payloads, 
and transaction audit trails.
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
    fk_status_id BINARY(16) NOT NULL,
    fk_gateway_id BINARY(16) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    gateway_transaction_id VARCHAR(255) NULL,
    gateway_order_id VARCHAR(255) NULL,
    gateway_payment_link_id VARCHAR(255) NULL,
    gateway_signature VARCHAR(500) NULL,
    error_message TEXT NULL,
    amount_paid DECIMAL(10,2) NOT NULL,
    paid_at_utc TIMESTAMP NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_payments_payment_id PRIMARY KEY (pk_payment_id),
    CONSTRAINT chk_payments_amount_positive CHECK (amount_paid >= 0),
    CONSTRAINT uq_payments_order UNIQUE (fk_order_id),
    
    CONSTRAINT fk_payments_order_id FOREIGN KEY (fk_order_id) REFERENCES orders(pk_order_id) ON DELETE RESTRICT,
    CONSTRAINT fk_payments_status_id FOREIGN KEY (fk_status_id) REFERENCES payment_statuses(pk_status_id) ON DELETE RESTRICT,
    CONSTRAINT fk_payments_gateway_id FOREIGN KEY (fk_gateway_id) REFERENCES payment_gateways(pk_gateway_id) ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_payments_order ON payments(fk_order_id);
CREATE INDEX idx_payments_status ON payments(fk_status_id);
CREATE INDEX idx_payments_gateway ON payments(fk_gateway_id);
CREATE INDEX idx_payments_method ON payments(payment_method);
CREATE INDEX idx_payments_gateway_transaction ON payments(gateway_transaction_id);
CREATE INDEX idx_payments_paid_at ON payments(paid_at_utc);
CREATE INDEX idx_payments_created_at ON payments(created_at_utc);
CREATE INDEX idx_payments_is_active ON payments(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE payments;

SHOW CREATE TABLE payments;
