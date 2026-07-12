/*
===============================================================================
Table       : payment_audit_logs
Description :
Immutable, append-only transaction history log tracking payment lifecycle updates,
state switches, administrative adjustments, and systemic trigger states.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS payment_audit_logs;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE payment_audit_logs
(
    pk_audit_log_id BINARY(16) NOT NULL,
    fk_payment_id BINARY(16) DEFAULT NULL,
    fk_refund_id BINARY(16) DEFAULT NULL,
    action VARCHAR(100) NOT NULL,
    fk_actor_id BINARY(16) DEFAULT NULL,
    actor_role VARCHAR(30) DEFAULT NULL,
    old_status VARCHAR(50) DEFAULT NULL,
    new_status VARCHAR(50) DEFAULT NULL,
    details JSON DEFAULT NULL,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),

    CONSTRAINT pk_audit_logs_id PRIMARY KEY (pk_audit_log_id),
    
    CONSTRAINT fk_audit_logs_payment_id FOREIGN KEY (fk_payment_id) REFERENCES payments(pk_payment_id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_logs_refund_id FOREIGN KEY (fk_refund_id) REFERENCES refunds(pk_refund_id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_logs_actor_id FOREIGN KEY (fk_actor_id) REFERENCES users(pk_user_id) ON DELETE SET NULL
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_audit_logs_payment ON payment_audit_logs(fk_payment_id);
CREATE INDEX idx_audit_logs_refund ON payment_audit_logs(fk_refund_id);
CREATE INDEX idx_audit_logs_action ON payment_audit_logs(action);
CREATE INDEX idx_audit_logs_created_at ON payment_audit_logs(created_at_utc);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE payment_audit_logs;

SHOW CREATE TABLE payment_audit_logs;
