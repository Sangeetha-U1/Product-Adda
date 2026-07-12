/*
===============================================================================
Table       : payment_reconciliation_logs
Description :
Logs the output of automatic and manual ledger settlement audits run against 
external payment gateway processing datasets.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS payment_reconciliation_logs;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE payment_reconciliation_logs
(
    pk_reconciliation_log_id BINARY(16) NOT NULL,
    reconciliation_type VARCHAR(30) NOT NULL,
    reconciliation_run_id BINARY(16) NOT NULL,
    fk_gateway_id BINARY(16) NOT NULL,
    total_gateway_transactions INT NOT NULL DEFAULT 0,
    total_db_records INT NOT NULL DEFAULT 0,
    matched_records INT NOT NULL DEFAULT 0,
    missing_in_db INT NOT NULL DEFAULT 0,
    orphaned_in_db INT NOT NULL DEFAULT 0,
    discrepancy_details JSON DEFAULT NULL,
    fk_status_id BINARY(16) NOT NULL,
    fk_triggered_by_user_id BINARY(16) DEFAULT NULL,
    reconciled_at_utc TIMESTAMP NULL DEFAULT NULL,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),

    CONSTRAINT pk_reconciliation_logs_id PRIMARY KEY (pk_reconciliation_log_id),
    
    CONSTRAINT fk_reconciliation_logs_gateway_id FOREIGN KEY (fk_gateway_id) REFERENCES payment_gateways(pk_gateway_id) ON DELETE RESTRICT,
    CONSTRAINT fk_reconciliation_logs_status_id FOREIGN KEY (fk_status_id) REFERENCES payment_statuses(pk_status_id) ON DELETE RESTRICT,
    CONSTRAINT fk_reconciliation_logs_triggered_by FOREIGN KEY (fk_triggered_by_user_id) REFERENCES users(pk_user_id) ON DELETE SET NULL
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_reconciliation_logs_gateway ON payment_reconciliation_logs(fk_gateway_id);
CREATE INDEX idx_reconciliation_logs_status ON payment_reconciliation_logs(fk_status_id);
CREATE INDEX idx_reconciliation_logs_run_id ON payment_reconciliation_logs(reconciliation_run_id);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE payment_reconciliation_logs;

SHOW CREATE TABLE payment_reconciliation_logs;
