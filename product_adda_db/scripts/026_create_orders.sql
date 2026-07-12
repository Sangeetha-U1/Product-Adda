/*
===============================================================================
Table       : orders
Description :
Stores customer order header information.
Represents a single order placed by a customer, tracking broken-down financial 
totals, promotional use, and idempotency guarantees.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS orders;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE orders
(
    pk_order_id BINARY(16) NOT NULL,
    order_number VARCHAR(50) NOT NULL,
    fk_user_id BINARY(16) NOT NULL,
    fk_cart_id BINARY(16) NULL,
    fk_status_id BINARY(16) NOT NULL,
    fk_address_id BINARY(16) NOT NULL,
    fk_coupon_id BINARY(16) NULL,
    fk_delivery_partner_id BINARY(16) NULL,
    fk_payment_id BINARY(16) DEFAULT NULL,
    payment_initiated_at_utc TIMESTAMP NULL DEFAULT NULL,
    payment_confirmed_at_utc TIMESTAMP NULL DEFAULT NULL,
    total_refunded_amount_in_paise BIGINT NOT NULL DEFAULT 0,
    subtotal DECIMAL(10,2) NOT NULL,
    coupon_discount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    shipping_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    idempotency_key BINARY(16) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()),
    updated_at_utc TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP()) ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_orders_order_id PRIMARY KEY (pk_order_id),
    CONSTRAINT uq_orders_order_number UNIQUE (order_number),
    CONSTRAINT uq_orders_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT fk_orders_user_id FOREIGN KEY (fk_user_id) REFERENCES users(pk_user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_cart_id FOREIGN KEY (fk_cart_id) REFERENCES carts(pk_cart_id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_status_id FOREIGN KEY (fk_status_id) REFERENCES order_statuses(pk_status_id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_address_id FOREIGN KEY (fk_address_id) REFERENCES addresses(pk_address_id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_coupon_id FOREIGN KEY (fk_coupon_id) REFERENCES coupons(pk_coupon_id) ON DELETE SET NULL,
    CONSTRAINT fk_orders_delivery_partner_id FOREIGN KEY (fk_delivery_partner_id) REFERENCES delivery_partners(pk_delivery_partner_id),
    CONSTRAINT fk_orders_payment_id FOREIGN KEY (fk_payment_id) REFERENCES payments(pk_payment_id) ON DELETE SET NULL,

    CONSTRAINT chk_orders_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_orders_coupon_discount CHECK (coupon_discount >= 0),
    CONSTRAINT chk_orders_shipping_cost CHECK (shipping_cost >= 0),
    CONSTRAINT chk_orders_tax_amount CHECK (tax_amount >= 0),
    CONSTRAINT chk_orders_total_amount CHECK (total_amount >= 0),
    CONSTRAINT chk_orders_total_refunded CHECK (total_refunded_amount_in_paise >= 0)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_user ON orders(fk_user_id);
CREATE INDEX idx_orders_cart ON orders(fk_cart_id);
CREATE INDEX idx_orders_status ON orders(fk_status_id);
CREATE INDEX idx_orders_address ON orders(fk_address_id);
CREATE INDEX idx_orders_coupon ON orders(fk_coupon_id);
CREATE INDEX idx_orders_total_amount ON orders(total_amount);
CREATE INDEX idx_orders_created_at ON orders(created_at_utc);
CREATE INDEX idx_orders_is_active ON orders(is_active);
CREATE INDEX idx_orders_delivery_partner_id ON orders(fk_delivery_partner_id);
CREATE INDEX idx_orders_payment_id ON orders(fk_payment_id);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE orders;

SHOW CREATE TABLE orders;