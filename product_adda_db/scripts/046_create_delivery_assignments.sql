/*
===============================================================================
Table       : delivery_assignments
Description :
Stores delivery partner assignment records for customer orders.
Tracks the complete assignment lifecycle from assignment through acceptance,
rejection, and delivery completion.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS delivery_assignments;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE delivery_assignments
(
    pk_assignment_id BINARY(16) NOT NULL,
    fk_order_id BINARY(16) NOT NULL,
    fk_delivery_partner_id BINARY(16) NOT NULL,
    fk_assignment_status_id BINARY(16) NOT NULL,
    assigned_at TIMESTAMP NOT NULL,
    accepted_at TIMESTAMP NULL,
    rejected_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    rejection_reason VARCHAR(500) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_delivery_assignments_assignment_id
        PRIMARY KEY (pk_assignment_id),

    CONSTRAINT fk_delivery_assignments_order_id
        FOREIGN KEY (fk_order_id)
        REFERENCES orders(pk_order_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_delivery_assignments_delivery_partner_id
        FOREIGN KEY (fk_delivery_partner_id)
        REFERENCES delivery_partners(pk_delivery_partner_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_delivery_assignments_assignment_status_id
        FOREIGN KEY (fk_assignment_status_id)
        REFERENCES delivery_assignment_statuses(pk_delivery_assignment_status_id)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_delivery_assignments_order
    ON delivery_assignments(fk_order_id);

CREATE INDEX idx_delivery_assignments_delivery_partner
    ON delivery_assignments(fk_delivery_partner_id);

CREATE INDEX idx_delivery_assignments_assignment_status_id
ON delivery_assignments(fk_assignment_status_id);

CREATE INDEX idx_delivery_assignments_assigned_at
    ON delivery_assignments(assigned_at);

CREATE INDEX idx_delivery_assignments_is_active
    ON delivery_assignments(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE delivery_assignments;

SHOW CREATE TABLE delivery_assignments;
