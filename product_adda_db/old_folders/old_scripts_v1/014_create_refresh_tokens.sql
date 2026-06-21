/*
===============================================================================
Table       : refresh_tokens
Description :
    Stores OAuth2/JWT refresh tokens for managing long-lived user sessions
    and handling token revocation.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS refresh_tokens
(
    pk_refresh_token_id  BINARY(16)    NOT NULL,
    fk_user_id           BINARY(16)    NOT NULL,
    
    token_hash           VARCHAR(255)  NOT NULL,
    
    expires_at_utc       DATETIME      NOT NULL,
    revoked_at_utc       DATETIME      NULL,
    created_at_utc       DATETIME      NOT NULL DEFAULT (UTC_TIMESTAMP()),

    CONSTRAINT pk_refresh_tokens_token_id
        PRIMARY KEY (pk_refresh_token_id),

    CONSTRAINT fk_refresh_tokens_users
        FOREIGN KEY (fk_user_id)
        REFERENCES users (pk_user_id)
        ON DELETE CASCADE,

    CONSTRAINT uq_refresh_tokens_token_hash
        UNIQUE (token_hash)
);

-- ============================================================================
-- Indexes
-- ============================================================================

-- Speeds up lookups when fetching or invalidating all tokens belonging to a specific user
CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens (fk_user_id);

-- Used by background cleanup cron jobs to purge expired tokens from the database
CREATE INDEX idx_refresh_tokens_expires_at
    ON refresh_tokens (expires_at_utc);


-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE refresh_tokens;

SHOW CREATE TABLE refresh_tokens;