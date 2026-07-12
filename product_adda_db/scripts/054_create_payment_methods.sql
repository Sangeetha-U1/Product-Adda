/*
===============================================================================
Table       : payment_methods
Description :
Stores tokenized customer payment methods (cards, UPI handles, wallets) linked
to gateway customer profiles. Does not store raw PAN/CVV data.
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
    fk_profile_id BINARY(16) NOT NULL,
    method_type VARCHAR(30) NOT NULL,
    gateway_token VARCHAR(255) NOT NULL,
    card_brand VARCHAR(30) NULL,
    card_last_four VARCHAR(4) NULL,
    card_expiry_month INT NULL,
    card_expiry_year INT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_payment_methods_id PRIMARY KEY (pk_payment_method_id),
    CONSTRAINT fk_payment_methods_profile_id FOREIGN KEY (fk_profile_id) REFERENCES customer_payment_profiles(pk_profile_id) ON DELETE CASCADE
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_payment_methods_profile ON payment_methods(fk_profile_id);
CREATE INDEX idx_payment_methods_type ON payment_methods(method_type);
CREATE INDEX idx_payment_methods_is_active ON payment_methods(is_active);

-- ============================================================================
-- Deferred Circular Constraints
-- ============================================================================

-- Add the missing link back from profile to its default payment method
ALTER TABLE customer_payment_profiles
    ADD CONSTRAINT fk_customer_payment_profiles_default_method 
    FOREIGN KEY (fk_default_payment_method_id) REFERENCES payment_methods(pk_payment_method_id) 
    ON DELETE SET NULL;

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE payment_methods;

SHOW CREATE TABLE payment_methods;
