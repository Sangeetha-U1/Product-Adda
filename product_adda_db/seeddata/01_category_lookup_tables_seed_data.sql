USE product_adda_db;

/*==============================================================
  UPGRADED UUID_V7 FUNCTION
==============================================================*/
DROP FUNCTION IF EXISTS UUID_V7;

DELIMITER $$

CREATE FUNCTION IF NOT EXISTS UUID_V7()
RETURNS BINARY(16)
DETERMINISTIC
BEGIN
    DECLARE v_time_ms  BIGINT;
    DECLARE v_time_hex VARCHAR(12);
    DECLARE v_uuid_hex VARCHAR(32);

    SET v_time_ms  = ROUND(UNIX_TIMESTAMP(SYSDATE(3)) * 1000);
    SET v_time_hex = LPAD(HEX(v_time_ms), 12, '0');

    SET v_uuid_hex = CONCAT(
        v_time_hex,
        '7',
        LPAD(HEX(FLOOR(RAND() * 4096)), 3, '0'),
        HEX(8 + FLOOR(RAND() * 4)),
        LPAD(HEX(FLOOR(RAND() * 4096)), 3, '0'),
        SUBSTRING(MD5(RAND()), 1, 12)
    );

    RETURN UNHEX(v_uuid_hex);
END$$

DELIMITER ;

/*==============================================================
CATEGORY 01 - LOOKUP TABLES
==============================================================*/

/*==============================================================
000. TRUNCATE
==============================================================*/
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `report_types`;
TRUNCATE TABLE `notification_channels`;
TRUNCATE TABLE `notification_types`;
TRUNCATE TABLE `inventory_transaction_types`;
TRUNCATE TABLE `address_types`;
TRUNCATE TABLE `payment_gateways`;
TRUNCATE TABLE `review_statuses`;
TRUNCATE TABLE `payment_statuses`;
TRUNCATE TABLE `order_statuses`;
TRUNCATE TABLE `roles`;

SET FOREIGN_KEY_CHECKS = 1;

/*==============================================================
001. SEED DATA FOR: roles
==============================================================*/

INSERT INTO roles (pk_role_id, role_name, description, is_active, created_at_utc) VALUES
(UUID_V7(), 'SUPER_ADMIN', 'Super Administrator role', TRUE, '2024-01-15 08:30:00'),
(UUID_V7(), 'ADMIN', 'Administrator role', TRUE, '2024-01-15 08:32:15'),
(UUID_V7(), 'USER', 'General User role', TRUE, '2024-01-16 10:00:00'),
(UUID_V7(), 'CUSTOMER', 'Customer role', TRUE, '2024-01-16 10:05:00'),
(UUID_V7(), 'VENDOR', 'Vendor role', TRUE, '2024-01-17 14:22:11');

/*==============================================================
002. SEED DATA FOR: order_statuses
==============================================================*/

INSERT INTO order_statuses (pk_status_id, status_name, description, is_active, created_at_utc) VALUES
(UUID_V7(), 'PENDING', 'Order created and awaiting confirmation', TRUE, '2024-02-01 06:00:00'),
(UUID_V7(), 'CONFIRMED', 'Order confirmed', TRUE, '2024-02-01 06:05:00'),
(UUID_V7(), 'PROCESSING', 'Order under processing', TRUE, '2024-02-01 06:15:00'),
(UUID_V7(), 'SHIPPED', 'Order shipped', TRUE, '2024-02-02 09:30:00'),
(UUID_V7(), 'DELIVERED', 'Order delivered', TRUE, '2024-02-03 12:00:00'),
(UUID_V7(), 'CANCELLED', 'Order cancelled', TRUE, '2024-02-04 15:45:10'),
(UUID_V7(), 'RETURNED', 'Order returned', TRUE, '2024-02-05 11:20:00'),
(UUID_V7(), 'REFUNDED', 'Order refunded', TRUE, '2024-02-06 16:10:22');

/*==============================================================
003. SEED DATA FOR: payment_statuses
==============================================================*/

INSERT INTO payment_statuses (pk_status_id, status_name, description, is_active, created_at_utc) VALUES
(UUID_V7(), 'PENDING', 'Payment pending', TRUE, '2024-03-01 10:00:00'),
(UUID_V7(), 'SUCCESS', 'Payment successful', TRUE, '2024-03-01 10:02:30'),
(UUID_V7(), 'FAILED', 'Payment failed', TRUE, '2024-03-01 10:05:12'),
(UUID_V7(), 'REFUNDED', 'Payment refunded', TRUE, '2024-03-02 14:40:00'),
(UUID_V7(), 'CANCELLED', 'Payment cancelled', TRUE, '2024-03-02 17:15:00');

/*==============================================================
004. SEED DATA FOR: review_statuses
==============================================================*/

INSERT INTO review_statuses (pk_status_id, status_name, description, is_active, created_at_utc) VALUES
(UUID_V7(), 'PENDING', 'Review awaiting moderation', TRUE, '2024-05-10 11:00:00'),
(UUID_V7(), 'APPROVED', 'Review approved', TRUE, '2024-05-10 13:30:00'),
(UUID_V7(), 'REJECTED', 'Review rejected', TRUE, '2024-05-11 09:15:25');

/*==============================================================
005. SEED DATA FOR: payment_gateways
==============================================================*/

INSERT INTO payment_gateways (pk_gateway_id, gateway_name, description, is_active, created_at_utc) VALUES
(UUID_V7(), 'RAZORPAY', 'Razorpay payment gateway', TRUE, '2024-08-20 07:00:00'),
(UUID_V7(), 'STRIPE', 'Stripe payment gateway', TRUE, '2024-08-20 07:05:00'),
(UUID_V7(), 'PAYPAL', 'PayPal payment gateway', TRUE, '2024-08-21 11:45:00');

/*==============================================================
006. SEED DATA FOR: address_types
==============================================================*/

INSERT INTO address_types (pk_address_type_id, address_type_name, description, is_active, created_at_utc) VALUES
(UUID_V7(), 'HOME', 'Home address', TRUE, '2024-11-01 09:00:00'),
(UUID_V7(), 'WORK', 'Work address', TRUE, '2024-11-01 09:02:00'),
(UUID_V7(), 'BILLING', 'Billing address', TRUE, '2024-11-01 09:04:00'),
(UUID_V7(), 'SHIPPING', 'Shipping address', TRUE, '2024-11-02 14:10:00'),
(UUID_V7(), 'BUSINESS', 'Business address', TRUE, '2024-11-03 10:30:15');

/*==============================================================
007. SEED DATA FOR: inventory_transaction_types
==============================================================*/

INSERT INTO inventory_transaction_types (pk_transaction_type_id, transaction_type_name, description, is_active, created_at_utc) VALUES
(UUID_V7(), 'STOCK_IN', 'Inventory stock added', TRUE, '2025-01-10 05:30:00'),
(UUID_V7(), 'STOCK_OUT', 'Inventory stock removed', TRUE, '2025-01-10 05:35:00'),
(UUID_V7(), 'RETURN', 'Inventory returned', TRUE, '2025-01-11 08:20:00'),
(UUID_V7(), 'ADJUSTMENT', 'Inventory adjusted', TRUE, '2025-01-12 13:14:55');

/*==============================================================
008. SEED DATA FOR: notification_types
==============================================================*/

INSERT INTO notification_types (pk_notification_type_id, notification_type_name, description, is_active, created_at_utc) VALUES
(UUID_V7(), 'REGISTRATION', 'User registration notification', TRUE, '2025-03-15 09:00:00'),
(UUID_V7(), 'ORDER_PLACED', 'Order placed notification', TRUE, '2025-03-15 09:02:00'),
(UUID_V7(), 'PAYMENT_SUCCESS', 'Payment success notification', TRUE, '2025-03-15 09:04:00'),
(UUID_V7(), 'ORDER_DELIVERED', 'Order delivered notification', TRUE, '2025-03-16 11:25:30');

/*==============================================================
009. SEED DATA FOR: notification_channels
==============================================================*/

INSERT INTO notification_channels (pk_channel_id, channel_name, description, is_active, created_at_utc) VALUES
(UUID_V7(), 'EMAIL', 'Email notification channel', TRUE, '2025-04-01 08:00:00'),
(UUID_V7(), 'SMS', 'SMS notification channel', TRUE, '2025-04-01 08:05:00'),
(UUID_V7(), 'PUSH', 'Push notification channel', TRUE, '2025-04-02 10:12:44');

/*==============================================================
010. SEED DATA FOR: report_types
==============================================================*/

INSERT INTO report_types (pk_report_type_id, report_type_name, description, is_active, created_at_utc) VALUES
(UUID_V7(), 'SALES', 'Sales report', TRUE, '2026-02-10 14:00:00'),
(UUID_V7(), 'ORDERS', 'Orders report', TRUE, '2026-02-11 09:30:00'),
(UUID_V7(), 'REVENUE', 'Revenue report', TRUE, '2026-03-01 11:15:00'),
(UUID_V7(), 'VENDOR_PERFORMANCE', 'Vendor performance report', TRUE, '2026-06-17 16:45:22');

-- ============================================================================
-- 035. SEED DATA FOR: product_status_lookup
-- ============================================================================

INSERT INTO product_status_lookup
    (pk_status_id, status_code, status_label, display_order, is_active)
VALUES
    (UUID_V7(),       'DRAFT',            'Draft',            1, TRUE),
    (UUID_V7(),       'PENDING_APPROVAL', 'Pending Approval',  2, TRUE),
    (UUID_V7(),       'APPROVED',         'Approved',          3, TRUE),
    (UUID_V7(),       'REJECTED',         'Rejected',          4, TRUE),
    (UUID_V7(),       'ARCHIVED',         'Archived',          5, TRUE);