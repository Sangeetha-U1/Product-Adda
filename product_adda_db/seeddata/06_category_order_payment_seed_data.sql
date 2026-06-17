USE product_adda_db;

/*==============================================================
000. TRUNCATE
==============================================================*/

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `payments`;
TRUNCATE TABLE `order_items`;
TRUNCATE TABLE `orders`;

SET FOREIGN_KEY_CHECKS = 1;

/*
===============================================================================
026. SEED DATA FOR: orders
===============================================================================
*/

INSERT INTO orders (pk_order_id, fk_user_id, fk_status_id, fk_address_id, total_amount, is_active, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'CONFIRMED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com') LIMIT 1), 131998.00, TRUE, '2025-05-01 14:00:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'DELIVERED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com') LIMIT 1), 1999.00, TRUE, '2025-05-02 11:30:00');

/*
===============================================================================
027. SEED DATA FOR: order_items
===============================================================================
*/

INSERT INTO order_items (pk_order_item_id, fk_order_id, fk_product_id, product_name_snapshot, quantity, unit_price, is_active, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_order_id FROM orders ORDER BY created_at_utc DESC LIMIT 1 OFFSET 1), (SELECT pk_product_id FROM products WHERE sku = 'MOB-SAMSUNG-S24-001'), 'Samsung Galaxy S24 Ultra', 1, 124999.00, TRUE, '2025-05-01 14:05:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders ORDER BY created_at_utc DESC LIMIT 1), (SELECT pk_product_id FROM products WHERE sku = 'MEN-NIKE-TSHIRT-001'), 'Nike Sports T-Shirt', 1, 1499.00, TRUE, '2025-05-02 11:35:00');

/*
===============================================================================
028. SEED DATA FOR: payments
===============================================================================
*/

INSERT INTO payments (pk_payment_id, fk_order_id, fk_status_id, fk_gateway_id, payment_method, gateway_transaction_id, gateway_order_id, gateway_signature, amount_paid, paid_at_utc, is_active, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_order_id FROM orders ORDER BY created_at_utc DESC LIMIT 1 OFFSET 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS'), (SELECT pk_gateway_id FROM payment_gateways WHERE gateway_name = 'RAZORPAY'), 'ONLINE', 'pay_test_razorpay_001', 'order_test_razorpay_001', 'signature_test_001', 131998.00, '2025-05-01 14:10:00', TRUE, '2025-05-01 14:10:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders ORDER BY created_at_utc DESC LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS'), (SELECT pk_gateway_id FROM payment_gateways WHERE gateway_name = 'STRIPE'), 'CARD', 'txn_stripe_test_001', 'order_stripe_test_001', 'signature_test_002', 1999.00, '2025-05-02 11:40:00', TRUE, '2025-05-02 11:40:00');
