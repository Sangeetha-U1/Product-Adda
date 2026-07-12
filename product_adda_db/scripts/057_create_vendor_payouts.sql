/*
===============================================================================
Table       : vendor_payouts
Description :
Tracks individual vendor payout generation, covering rolling calculation cycles, 
platform commissions, net transfers, and gateway transfer statuses.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS vendor_payouts;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE vendor_payouts
(
    pk_vendor_payout_id BINARY(16) NOT NULL,
    fk_vendor_id BINARY(16) NOT NULL,
    period_start_date DATE NOT NULL,
    period_end_date DATE NOT NULL,
    total_amount_in_paise BIGINT NOT NULL,
    platform_commission_in_paise BIGINT NOT NULL,
    net_payout_amount_in_paise BIGINT NOT NULL,
    fk_status_id BINARY(16) NOT NULL,
    scheduled_payout_date DATE NOT NULL,
    actual_payout_at_utc TIMESTAMP NULL DEFAULT NULL,
    payout_method VARCHAR(50) DEFAULT NULL,
    payout_reference_id VARCHAR(255) DEFAULT NULL,
    failure_reason TEXT NULL,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_vendor_payouts_id PRIMARY KEY (pk_vendor_payout_id),
    CONSTRAINT chk_vendor_payouts_net_amount CHECK (net_payout_amount_in_paise >= 0),
    CONSTRAINT chk_vendor_payouts_period CHECK (period_end_date >= period_start_date),
    
    CONSTRAINT fk_vendor_payouts_vendor_id FOREIGN KEY (fk_vendor_id) REFERENCES vendors(pk_vendor_id) ON DELETE RESTRICT,
    CONSTRAINT fk_vendor_payouts_status_id FOREIGN KEY (fk_status_id) REFERENCES payment_statuses(pk_status_id) ON DELETE RESTRICT
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_vendor_payouts_vendor ON vendor_payouts(fk_vendor_id);
CREATE INDEX idx_vendor_payouts_status ON vendor_payouts(fk_status_id);
CREATE INDEX idx_vendor_payouts_scheduled_date ON vendor_payouts(scheduled_payout_date);
CREATE INDEX idx_vendor_payouts_period ON vendor_payouts(period_start_date, period_end_date);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE vendor_payouts;

SHOW CREATE TABLE vendor_payouts;
