/*
===============================================================================
Table       : customer_payment_profiles
Description :
Stores customer payment profile information.

Maintains customer payment preferences and aggregated payment statistics.
Stores the customer's preferred saved payment method while keeping payment
method details in the payment_methods table.
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

    fk_preferred_payment_method_id BINARY(16) NULL,

    total_spent_in_paise BIGINT NOT NULL DEFAULT 0,

    total_refunded_in_paise BIGINT NOT NULL DEFAULT 0,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_customer_payment_profiles_id
        PRIMARY KEY (pk_profile_id),

    CONSTRAINT uq_customer_payment_profiles_user
        UNIQUE (fk_user_id),

    CONSTRAINT fk_customer_payment_profiles_user_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_customer_payment_profiles_preferred_method
        FOREIGN KEY (fk_preferred_payment_method_id)
        REFERENCES payment_methods(pk_payment_method_id)
        ON DELETE SET NULL,

    CONSTRAINT chk_customer_payment_profiles_total_spent
        CHECK (total_spent_in_paise >= 0),

    CONSTRAINT chk_customer_payment_profiles_total_refunded
        CHECK (total_refunded_in_paise >= 0)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_customer_payment_profiles_preferred_method
    ON customer_payment_profiles(fk_preferred_payment_method_id);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE customer_payment_profiles;

SHOW CREATE TABLE customer_payment_profiles;
