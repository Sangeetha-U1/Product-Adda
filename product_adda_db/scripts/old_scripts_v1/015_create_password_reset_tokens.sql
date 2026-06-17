/*
===============================================================================
Table       : password_reset_tokens
Description :
    Stores password reset tokens for securely managing user password recovery
    workflow and one-time password update authorization.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS password_reset_tokens
(
    pk_reset_token_id     BINARY(16)    NOT NULL,
    fk_user_id            BINARY(16)    NOT NULL,

    token_hash            VARCHAR(255)  NOT NULL,

    expires_at_utc        DATETIME      NOT NULL,

    is_used               BOOLEAN       NOT NULL DEFAULT FALSE,

    created_at_utc        DATETIME      NOT NULL DEFAULT (UTC_TIMESTAMP()),


    CONSTRAINT pk_password_reset_tokens_token_id
        PRIMARY KEY (pk_reset_token_id),


    CONSTRAINT fk_password_reset_tokens_users
        FOREIGN KEY (fk_user_id)
        REFERENCES users (pk_user_id)
        ON DELETE CASCADE,


    CONSTRAINT uq_password_reset_tokens_token_hash
        UNIQUE (token_hash)
);


-- ============================================================================
-- Indexes
-- ============================================================================

-- Speeds up deleting old reset tokens for a user before creating a new token
CREATE INDEX idx_password_reset_tokens_user_id
    ON password_reset_tokens (fk_user_id);


-- Used by cleanup jobs to remove expired password reset tokens
CREATE INDEX idx_password_reset_tokens_expires_at
    ON password_reset_tokens (expires_at_utc);



-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE password_reset_tokens;

SHOW CREATE TABLE password_reset_tokens;
