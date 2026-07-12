/*
===============================================================================
Table       : customer_payment_profiles
Description :
Stores customer vaults and reference keys for saved payment methods.
Links securely back to the user profiles without holding raw sensitive card
data locally.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS customer_payment_profiles;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE customer_payment_profiles
(
    pk_profile_id BINARY(16) NOT NULL,
    fk_user_id BINARY(16) NOT NULL,
    fk_default_payment_method_id BINARY(16) NULL, -- Kept NULL initially due to circular FK dependency
    gateway_customer_id VARCHAR(255) NOT NULL,
    fk_gateway_id BINARY(16) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_customer_payment_profiles_id PRIMARY KEY (pk_profile_id),
    CONSTRAINT uq_customer_payment_profiles_gateway UNIQUE (fk_gateway_id, gateway_customer_id),
    
    CONSTRAINT fk_customer_payment_profiles_user_id FOREIGN KEY (fk_user_id) REFERENCES users(pk_user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_customer_payment_profiles_gateway_id FOREIGN KEY (fk_gateway_id) REFERENCES payment_gateways(pk_gateway_id) ON DELETE RESTRICT
    -- Note: Circular FK constraint to payment_methods is added via ALTER in the payment_methods script
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_customer_payment_profiles_user ON customer_payment_profiles(fk_user_id);
CREATE INDEX idx_customer_payment_profiles_gateway ON customer_payment_profiles(fk_gateway_id);
CREATE INDEX idx_customer_payment_profiles_is_active ON customer_payment_profiles(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE customer_payment_profiles;

SHOW CREATE TABLE customer_payment_profiles;
