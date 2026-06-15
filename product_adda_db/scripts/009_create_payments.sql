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
		pk_payment_id         BINARY(16)        NOT NULL,
		fk_order_id           BINARY(16)        NOT NULL,

		payment_method        VARCHAR(50)       NOT NULL,
        fk_status_id         BINARY(16)        NOT NULL,

		razorpay_order_id     VARCHAR(255)      NULL,
		razorpay_payment_id   VARCHAR(255)      NULL,
        razorpay_payment_link_id VARCHAR(255) 	NULL,
		razorpay_signature    VARCHAR(500)      NULL,

		amount_paid           DECIMAL(10,2)     NOT NULL,

		paid_at_utc               TIMESTAMP         NULL,
        
        created_at_utc    TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),

		CONSTRAINT pk_payments_payment_id
			PRIMARY KEY (pk_payment_id),

		CONSTRAINT fk_payments_order_id
			FOREIGN KEY (fk_order_id)
			REFERENCES orders(pk_order_id)
			ON DELETE CASCADE,
            
		CONSTRAINT fk_payments_status_id FOREIGN KEY (fk_status_id) REFERENCES payment_statuses(pk_status_id)
	);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_payments_order_id
    ON payments(fk_order_id);

CREATE INDEX idx_payments_status_id ON payments(fk_status_id);

CREATE INDEX idx_payments_payment_method
    ON payments(payment_method);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE payments;

SHOW CREATE TABLE payments;