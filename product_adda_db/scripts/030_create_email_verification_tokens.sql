/*
===============================================================================
Table       : email_verification_tokens
Description :
Stores email verification tokens generated during user registration.
Supports email verification and resend verification workflow.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS email_verification_tokens;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE email_verification_tokens
(
    pk_verification_token_id BINARY(16) NOT NULL,

    fk_user_id BINARY(16) NOT NULL,

    verification_token VARCHAR(255) NOT NULL,

    expires_at_utc TIMESTAMP NOT NULL,

    is_used BOOLEAN NOT NULL DEFAULT FALSE,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,


    CONSTRAINT pk_email_verification_tokens_id
        PRIMARY KEY (pk_verification_token_id),


    CONSTRAINT fk_email_verification_tokens_user_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE CASCADE,


    CONSTRAINT uq_email_verification_tokens_token
        UNIQUE (verification_token)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_email_verification_tokens_user_id
ON email_verification_tokens(fk_user_id);


CREATE INDEX idx_email_verification_tokens_expires_at
ON email_verification_tokens(expires_at_utc);


CREATE INDEX idx_email_verification_tokens_is_used
ON email_verification_tokens(is_used);


CREATE INDEX idx_email_verification_tokens_is_active
ON email_verification_tokens(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE email_verification_tokens;

SHOW CREATE TABLE email_verification_tokens;