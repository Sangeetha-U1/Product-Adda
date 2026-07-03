/*
===============================================================================
Table       : inventory_reservations
Description :
Stores temporary inventory reservations for products added to carts.
Reservations automatically expire if checkout is not completed.

Seed Data
No seed data required.
This is a transactional table. Do not add it to any seed file.

===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS inventory_reservations;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE inventory_reservations
(
    pk_reservation_id BINARY(16) NOT NULL,
    fk_product_id BINARY(16) NOT NULL,
    fk_cart_id BINARY(16) NOT NULL,
    fk_cart_item_id BINARY(16) NOT NULL,
    reserved_quantity INT NOT NULL,
    expires_at_utc TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_inventory_reservations_reservation_id PRIMARY KEY (pk_reservation_id),
    CONSTRAINT fk_inventory_reservations_product_id FOREIGN KEY (fk_product_id) REFERENCES products(pk_product_id) ON DELETE RESTRICT,
    CONSTRAINT fk_inventory_reservations_cart_id FOREIGN KEY (fk_cart_id) REFERENCES carts(pk_cart_id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_reservations_cart_item_id FOREIGN KEY (fk_cart_item_id) REFERENCES cart_items(pk_cart_item_id) ON DELETE CASCADE,
    CONSTRAINT chk_inventory_reservations_reserved_quantity_positive CHECK (reserved_quantity > 0)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_inventory_reservations_product ON inventory_reservations(fk_product_id);
CREATE INDEX idx_inventory_reservations_cart ON inventory_reservations(fk_cart_id);
CREATE INDEX idx_inventory_reservations_cart_item ON inventory_reservations(fk_cart_item_id);
CREATE INDEX idx_inventory_reservations_expires_at ON inventory_reservations(expires_at_utc);
CREATE INDEX idx_inventory_reservations_is_active ON inventory_reservations(is_active);
CREATE INDEX idx_inventory_reservations_created_at ON inventory_reservations(created_at_utc);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE inventory_reservations;

SHOW CREATE TABLE inventory_reservations;