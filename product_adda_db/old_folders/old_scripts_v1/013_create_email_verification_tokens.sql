/*
===============================================================================
Table       : email_verification_tokens
Description :
    Stores email verification tokens used for account activation.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS email_verification_tokens
(
    pk_verification_token_id BINARY(16)    NOT NULL,

    fk_user_id              BINARY(16)    NOT NULL,

    verification_token      VARCHAR(255)  NOT NULL,

    expires_at_utc          TIMESTAMP     NOT NULL,

    is_used                 TINYINT(1)    NOT NULL DEFAULT 0,

    created_at_utc          TIMESTAMP     NOT NULL DEFAULT (UTC_TIMESTAMP()),

    CONSTRAINT pk_email_verification_tokens_verification_token_id
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

CREATE INDEX idx_email_verification_tokens_fk_user_id
    ON email_verification_tokens(fk_user_id);

CREATE INDEX idx_email_verification_tokens_token
    ON email_verification_tokens(verification_token);

CREATE INDEX idx_email_verification_tokens_is_used
    ON email_verification_tokens(is_used);

CREATE INDEX idx_email_verification_tokens_expires_at_utc
    ON email_verification_tokens(expires_at_utc);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE email_verification_tokens;

SHOW CREATE TABLE email_verification_tokens;