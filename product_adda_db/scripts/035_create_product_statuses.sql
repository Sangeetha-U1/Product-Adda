/*
===============================================================================
Table       : product_statuses
Description :
    Lookup table defining all valid product lifecycle states.

    Status Flow:
    DRAFT → PENDING_APPROVAL → APPROVED → REJECTED → ARCHIVED

    Referenced by the `products` table via `fk_status` foreign key.
    Admin-only write operations. Application reads only.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Drop Table
-- ============================================================================

DROP TABLE IF EXISTS product_statuses;

-- ============================================================================
-- Create Table
-- ============================================================================
product_statuses
CREATE TABLE product_statuses
(
    pk_status_id    BINARY(16) NOT NULL,

    status_code     VARCHAR(50)     NOT NULL,

    status_label    VARCHAR(100)    NOT NULL,

    display_order   INT             NOT NULL DEFAULT 0,

    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,

    created_at_utc  TIMESTAMP       NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc  TIMESTAMP       NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_product_statuses_pk_status_id
        PRIMARY KEY (pk_status_id),

    CONSTRAINT uq_product_statuses_status_code
        UNIQUE (status_code)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_product_statuses_status_code
    ON product_statuses(status_code);

CREATE INDEX idx_product_statuses_is_active
    ON product_statuses(is_active);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE product_statuses;

SHOW CREATE TABLE product_statuses;