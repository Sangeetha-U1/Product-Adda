/*
===============================================================================
Table       : refresh_tokens
Description :
Stores hashed JWT refresh tokens.
Raw refresh tokens are never stored.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS refresh_tokens;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE refresh_tokens
(
    pk_refresh_token_id BINARY(16) NOT NULL,

    fk_user_id BINARY(16) NOT NULL,

    token_hash VARCHAR(255) NOT NULL,

    expires_at_utc DATETIME NOT NULL,

    revoked_at_utc DATETIME NULL,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,


    CONSTRAINT pk_refresh_tokens_id
        PRIMARY KEY(pk_refresh_token_id),


    CONSTRAINT fk_refresh_tokens_user_id
        FOREIGN KEY(fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE CASCADE,


    CONSTRAINT uq_refresh_tokens_token_hash
        UNIQUE(token_hash)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_refresh_tokens_user_id
ON refresh_tokens(fk_user_id);


CREATE INDEX idx_refresh_tokens_expires_at
ON refresh_tokens(expires_at_utc);


CREATE INDEX idx_refresh_tokens_is_active
ON refresh_tokens(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE refresh_tokens;

SHOW CREATE TABLE refresh_tokens;