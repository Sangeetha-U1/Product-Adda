/*
===============================================================================
Table       : delivery_partners
Description :
Stores delivery partner master information.
Represents delivery personnel responsible for fulfilling customer orders,
including delivery capacity, availability, ratings, and profile information.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS delivery_partners;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE delivery_partners
(
    pk_delivery_partner_id BINARY(16) NOT NULL,
    fk_status_id BINARY(16) NOT NULL,
    partner_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    current_location VARCHAR(500) NULL,
    max_concurrent_deliveries INT NOT NULL,
    active_deliveries INT NOT NULL DEFAULT 0,
    rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    total_deliveries INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_delivery_partners_delivery_partner_id
        PRIMARY KEY (pk_delivery_partner_id),

    CONSTRAINT fk_delivery_partners_status_id
        FOREIGN KEY (fk_status_id)
        REFERENCES delivery_partner_statuses(pk_delivery_partner_status_id),

    CONSTRAINT uq_delivery_partners_email
        UNIQUE (email),

    CONSTRAINT chk_delivery_partners_max_concurrent_deliveries
        CHECK (max_concurrent_deliveries > 0),

    CONSTRAINT chk_delivery_partners_active_deliveries
        CHECK (active_deliveries >= 0),

    CONSTRAINT chk_delivery_partners_rating
        CHECK (rating >= 0 AND rating <= 5),

    CONSTRAINT chk_delivery_partners_total_deliveries
        CHECK (total_deliveries >= 0)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_delivery_partners_status_id
ON delivery_partners(fk_status_id);

CREATE INDEX idx_delivery_partners_email
    ON delivery_partners(email);

CREATE INDEX idx_delivery_partners_joined_at
    ON delivery_partners(joined_at);

CREATE INDEX idx_delivery_partners_is_active
    ON delivery_partners(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE delivery_partners;

SHOW CREATE TABLE delivery_partners;


