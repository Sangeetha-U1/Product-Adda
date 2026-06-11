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
    transaction_id   BIGINT AUTO_INCREMENT NOT NULL,

    payment_status   VARCHAR(50)           NOT NULL,

    CONSTRAINT pk_payments
        PRIMARY KEY (transaction_id)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_payments_status
    ON payments(payment_status);

-- ============================================================================
-- Table Verification
-- ============================================================================

DESCRIBE payments;

SHOW CREATE TABLE payments;