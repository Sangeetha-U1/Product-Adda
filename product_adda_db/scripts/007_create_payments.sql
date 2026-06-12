/*
===============================================================================
Table       : payments
Description :
    Stores payment transaction details for orders.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS payments
(
    pk_payment_id     BINARY(16)        NOT NULL,
    fk_order_id       BINARY(16)        NOT NULL,

    payment_method    VARCHAR(50)       NOT NULL,
    payment_status    VARCHAR(50)       NOT NULL,

    transaction_ref   VARCHAR(255)      NULL,
    amount_paid       DECIMAL(10,2)     NOT NULL,

    paid_at           TIMESTAMP         NULL,

    CONSTRAINT pk_payments_payment_id
        PRIMARY KEY (pk_payment_id),

    CONSTRAINT fk_payments_order_id
        FOREIGN KEY (fk_order_id)
        REFERENCES orders(pk_order_id)
        ON DELETE CASCADE
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_payments_order_id
    ON payments(fk_order_id);

CREATE INDEX idx_payments_status
    ON payments(payment_status);

CREATE INDEX idx_payments_method
    ON payments(payment_method);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE payments;

SHOW CREATE TABLE payments;