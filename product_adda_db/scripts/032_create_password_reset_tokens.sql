/*
===============================================================================
Table       : password_reset_tokens
Description :
Stores password recovery tokens.
Only token hashes are stored.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS password_reset_tokens;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE password_reset_tokens
(
    pk_reset_token_id BINARY(16) NOT NULL,

    fk_user_id BINARY(16) NOT NULL,

    token_hash VARCHAR(255) NOT NULL,

    expires_at_utc DATETIME NOT NULL,

    is_used BOOLEAN NOT NULL DEFAULT FALSE,


    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),


    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,


    is_active BOOLEAN NOT NULL DEFAULT TRUE,


    CONSTRAINT pk_password_reset_tokens_id
        PRIMARY KEY(pk_reset_token_id),


    CONSTRAINT fk_password_reset_tokens_user_id
        FOREIGN KEY(fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE CASCADE,


    CONSTRAINT uq_password_reset_tokens_token_hash
        UNIQUE(token_hash)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_password_reset_tokens_user_id
ON password_reset_tokens(fk_user_id);


CREATE INDEX idx_password_reset_tokens_expires_at
ON password_reset_tokens(expires_at_utc);


CREATE INDEX idx_password_reset_tokens_is_used
ON password_reset_tokens(is_used);


CREATE INDEX idx_password_reset_tokens_is_active
ON password_reset_tokens(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE password_reset_tokens;

SHOW CREATE TABLE password_reset_tokens;
