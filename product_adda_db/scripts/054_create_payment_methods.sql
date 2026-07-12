/*
===============================================================================
Table       : payment_methods
Description :
Stores tokenized customer payment methods.

Contains references to gateway-managed payment tokens (cards, UPI, wallets,
etc.) without storing sensitive payment information such as PAN or CVV.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS payment_methods;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE payment_methods
(
    pk_payment_method_id BINARY(16) NOT NULL,

    fk_user_id BINARY(16) NOT NULL,

    payment_gateway VARCHAR(50) NOT NULL,

    gateway_token_id VARCHAR(255) NOT NULL,

    method_type VARCHAR(30) NOT NULL,

    card_last_four VARCHAR(4) NULL,

    card_brand VARCHAR(30) NULL,

    card_expiry_month TINYINT NULL,

    card_expiry_year SMALLINT NULL,

    is_primary BOOLEAN NOT NULL DEFAULT FALSE,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_payment_methods_id
        PRIMARY KEY (pk_payment_method_id),

    CONSTRAINT uq_payment_methods_user_token
        UNIQUE (fk_user_id, gateway_token_id),

    CONSTRAINT fk_payment_methods_user_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_payment_methods_user
    ON payment_methods(fk_user_id);

CREATE INDEX idx_payment_methods_is_active
    ON payment_methods(is_active);

CREATE INDEX idx_payment_methods_is_primary
    ON payment_methods(is_primary);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE payment_methods;

SHOW CREATE TABLE payment_methods;
