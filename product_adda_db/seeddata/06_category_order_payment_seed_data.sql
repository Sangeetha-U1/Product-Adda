USE product_adda_db;

/*==============================================================
000. TRUNCATE
==============================================================*/

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `tax_configurations`;
TRUNCATE TABLE `shipping_methods`;
TRUNCATE TABLE `coupons`;
TRUNCATE TABLE `payments`;
TRUNCATE TABLE `order_items`;
TRUNCATE TABLE `orders`;

SET FOREIGN_KEY_CHECKS = 1;

/*
===============================================================================
026. SEED DATA FOR: orders
===============================================================================
*/

INSERT INTO orders (pk_order_id, order_number, fk_user_id, fk_cart_id, fk_status_id, fk_address_id, fk_coupon_id, subtotal, coupon_discount, shipping_cost, tax_amount, total_amount, idempotency_key, is_active, created_at_utc) VALUES
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'CONFIRMED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com') LIMIT 1), NULL, 131998.00, 0.00, 0.00, 0.00, 131998.00, UUID_V7(), TRUE, '2025-05-01 14:00:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'DELIVERED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com') LIMIT 1), NULL, 1999.00, 0.00, 0.00, 0.00, 1999.00, UUID_V7(), TRUE, '2025-05-02 11:30:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'superadmin@productadda.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PROCESSING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'superadmin@productadda.com') LIMIT 1), NULL, 45000.00, 0.00, 0.00, 0.00, 45000.00, UUID_V7(), TRUE, '2025-05-21 10:00:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'SHIPPED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com') LIMIT 1), NULL, 1500.00, 0.00, 0.00, 0.00, 1500.00, UUID_V7(), TRUE, '2025-05-21 11:30:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'rajesh@techsolutions.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PENDING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'rajesh@techsolutions.com') LIMIT 1), NULL, 89999.00, 0.00, 0.00, 0.00, 89999.00, UUID_V7(), TRUE, '2025-05-22 14:15:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'anita@fashionhub.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'CONFIRMED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'anita@fashionhub.com') LIMIT 1), NULL, 12450.00, 0.00, 0.00, 0.00, 12450.00, UUID_V7(), TRUE, '2025-05-22 16:00:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'superadmin@productadda.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'CANCELLED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'superadmin@productadda.com') LIMIT 1), NULL, 3200.00, 0.00, 0.00, 0.00, 3200.00, UUID_V7(), TRUE, '2025-05-23 09:45:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'RETURNED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com') LIMIT 1), NULL, 750.00, 0.00, 0.00, 0.00, 750.00, UUID_V7(), TRUE, '2025-05-23 13:20:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'rajesh@techsolutions.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'REFUNDED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'rajesh@techsolutions.com') LIMIT 1), NULL, 54000.00, 0.00, 0.00, 0.00, 54000.00, UUID_V7(), TRUE, '2025-05-24 11:10:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'anita@fashionhub.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'DELIVERED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'anita@fashionhub.com') LIMIT 1), NULL, 6800.00, 0.00, 0.00, 0.00, 6800.00, UUID_V7(), TRUE, '2025-05-24 17:05:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'vikram.m@gmail.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PROCESSING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'vikram.m@gmail.com') LIMIT 1), NULL, 999.00, 0.00, 0.00, 0.00, 999.00, UUID_V7(), TRUE, '2025-05-25 08:30:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'SHIPPED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com') LIMIT 1), NULL, 4200.00, 0.00, 0.00, 0.00, 4200.00, UUID_V7(), TRUE, '2025-05-25 12:00:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'CANCELLED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com') LIMIT 1), NULL, 1350.00, 0.00, 0.00, 0.00, 1350.00, UUID_V7(), TRUE, '2025-05-26 15:30:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'vikram.m@gmail.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'DELIVERED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'vikram.m@gmail.com') LIMIT 1), NULL, 27500.00, 0.00, 0.00, 0.00, 27500.00, UUID_V7(), TRUE, '2025-05-26 19:10:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'superadmin@productadda.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PENDING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'superadmin@productadda.com') LIMIT 1), NULL, 11500.00, 0.00, 0.00, 0.00, 11500.00, UUID_V7(), TRUE, '2025-05-27 09:00:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PENDING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com') LIMIT 1), NULL, 2450.00, 0.00, 0.00, 0.00, 2450.00, UUID_V7(), TRUE, '2025-05-27 10:30:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'anita@fashionhub.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PENDING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'anita@fashionhub.com') LIMIT 1), NULL, 38200.00, 0.00, 0.00, 0.00, 38200.00, UUID_V7(), TRUE, '2025-05-27 11:15:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PENDING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com') LIMIT 1), NULL, 1899.00, 0.00, 0.00, 0.00, 1899.00, UUID_V7(), TRUE, '2025-05-27 14:00:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PENDING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com') LIMIT 1), NULL, 4750.00, 0.00, 0.00, 0.00, 4750.00, UUID_V7(), TRUE, '2025-05-27 15:45:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'vikram.m@gmail.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PENDING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'vikram.m@gmail.com') LIMIT 1), NULL, 63900.00, 0.00, 0.00, 0.00, 63900.00, UUID_V7(), TRUE, '2025-05-27 16:20:00'),
(UUID_V7(), CONCAT('ORD-', UPPER(RIGHT(HEX(UUID_V7()), 12))), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), NULL, (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PENDING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com') LIMIT 1), NULL, 850.00, 0.00, 0.00, 0.00, 850.00, UUID_V7(), TRUE, '2025-05-27 18:10:00');


/*
===============================================================================
027. SEED DATA FOR: order_items
===============================================================================
*/

INSERT INTO order_items (pk_order_item_id, fk_order_id, fk_product_id, fk_vendor_id, product_name_snapshot, quantity, unit_price, line_total, is_active, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 131998.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Samsung Galaxy S24 Ultra', 1, 124999.00, 124999.00, TRUE, '2025-05-01 14:05:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 1999.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1 OFFSET 1), (SELECT fk_vendor_id FROM products LIMIT 1 OFFSET 1), 'Nike Sports T-Shirt', 1, 1499.00, 1499.00, TRUE, '2025-05-02 11:35:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 45000.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Office Desk & Ergonomic Chair Combo', 1, 45000.00, 45000.00, TRUE, '2025-05-21 10:05:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 1500.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Wireless Testing Mouse', 1, 1500.00, 1500.00, TRUE, '2025-05-21 11:35:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 89999.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Wholesale Component Batch', 1, 89999.00, 89999.00, TRUE, '2025-05-22 14:20:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 12450.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Boutique Fabric Assortment', 1, 12450.00, 12450.00, TRUE, '2025-05-22 16:05:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 3200.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Misordered Standing Frame', 1, 3200.00, 3200.00, TRUE, '2025-05-23 09:50:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 750.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Faulty USB-C Hub Extension', 1, 750.00, 750.00, TRUE, '2025-05-23 13:25:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 54000.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Bulk Monitor Shipment', 1, 54000.00, 54000.00, TRUE, '2025-05-24 11:15:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 6800.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Designer Dress Mannequins', 1, 6800.00, 6800.00, TRUE, '2025-05-24 17:10:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 999.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Premium Tempered Glass Protector', 1, 999.00, 999.00, TRUE, '2025-05-25 08:35:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 4200.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Leather Casual Jacket', 1, 4200.00, 4200.00, TRUE, '2025-05-25 12:05:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 1350.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Handmade Ceramic Flower Vase', 1, 1350.00, 1350.00, TRUE, '2025-05-26 15:35:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 27500.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Mechanical Gaming Keyboard Setup', 1, 27500.00, 27500.00, TRUE, '2025-05-26 19:15:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 11500.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Server Rack Management Shelf', 1, 11500.00, 11500.00, TRUE, '2025-05-27 09:05:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 2450.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Braided Cat6 Ethernet Reel', 1, 2450.00, 2450.00, TRUE, '2025-05-27 10:35:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 38200.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Industrial Sewing Stitcher Pack', 1, 38200.00, 38200.00, TRUE, '2025-05-27 11:20:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 1899.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'MagSafe Powerbank Pack', 1, 1899.00, 1899.00, TRUE, '2025-05-27 14:05:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 4750.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Bohemian Living Room Rug', 1, 4750.00, 4750.00, TRUE, '2025-05-27 15:50:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 63900.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), '4K Ultra-Wide Desk Display', 1, 63900.00, 63900.00, TRUE, '2025-05-27 16:25:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 850.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), (SELECT fk_vendor_id FROM products LIMIT 1), 'Braided Multi-USB Cable Set', 1, 850.00, 850.00, TRUE, '2025-05-27 18:15:00');

/*
===============================================================================
028. SEED DATA FOR: payments
===============================================================================
*/

INSERT INTO payments (pk_payment_id, fk_order_id, fk_status_id, fk_gateway_id, payment_method, gateway_transaction_id, gateway_order_id, gateway_signature, error_message, amount_paid, paid_at_utc, is_active, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 131998.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways WHERE gateway_name LIKE '%RAZORPAY%' LIMIT 1), 'ONLINE', 'pay_test_razorpay_001', 'order_test_razorpay_001', 'signature_test_001', NULL, 131998.00, '2025-05-01 14:10:00', TRUE, '2025-05-01 14:10:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 1999.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways WHERE gateway_name LIKE '%STRIPE%' LIMIT 1), 'CARD', 'txn_stripe_test_001', 'order_stripe_test_001', 'signature_test_002', NULL, 1999.00, '2025-05-02 11:40:00', TRUE, '2025-05-02 11:40:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 45000.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'ONLINE', 'pay_v7_001', 'go_v7_001', 'sig_v7_001', NULL, 45000.00, '2025-05-21 10:02:00', TRUE, '2025-05-21 10:02:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 1500.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'CARD', 'pay_v7_002', 'go_v7_002', 'sig_v7_002', NULL, 1500.00, '2025-05-21 11:32:00', TRUE, '2025-05-21 11:32:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 12450.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'ONLINE', 'pay_v7_004', 'go_v7_004', 'sig_v7_004', NULL, 12450.00, '2025-05-22 16:02:00', TRUE, '2025-05-22 16:02:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 3200.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name LIKE '%FAIL%' OR status_name LIKE '%CANCEL%' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'ONLINE', 'pay_v7_005', 'go_v7_005', NULL, 'BAD_GATEWAY_RESPONSE', 0.00, NULL, TRUE, '2025-05-23 09:47:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 750.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'CARD', 'pay_v7_006', 'go_v7_006', 'sig_v7_006', NULL, 750.00, '2025-05-23 13:22:00', TRUE, '2025-05-23 13:22:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 54000.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name LIKE '%REFUND%' OR status_name LIKE '%REVERSED%' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'ONLINE', 'pay_v7_007', 'go_v7_007', 'sig_v7_007', NULL, 54000.00, '2025-05-24 11:12:00', TRUE, '2025-05-24 11:12:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 6800.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'ONLINE', 'pay_v7_008', 'go_v7_008', 'sig_v7_008', NULL, 6800.00, '2025-05-24 17:07:00', TRUE, '2025-05-24 17:07:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 999.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'CARD', 'pay_v7_009', 'go_v7_009', 'sig_v7_009', NULL, 999.00, '2025-05-25 08:32:00', TRUE, '2025-05-25 08:32:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 4200.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'ONLINE', 'pay_v7_010', 'go_v7_010', 'sig_v7_010', NULL, 4200.00, '2025-05-25 12:02:00', TRUE, '2025-05-25 12:02:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 1350.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name LIKE '%FAIL%' OR status_name LIKE '%CANCEL%' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'ONLINE', 'pay_v7_011', 'go_v7_011', NULL, 'USER_ABANDONED_TRANSACTION', 0.00, NULL, TRUE, '2025-05-26 15:32:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 27500.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'CARD', 'pay_v7_012', 'go_v7_012', 'sig_v7_012', NULL, 27500.00, '2025-05-26 19:12:00', TRUE, '2025-05-26 19:12:00');

/*==============================================================
041. SEED DATA FOR: coupons
==============================================================*/

INSERT INTO coupons (pk_coupon_id, coupon_code, description, fk_discount_type_id, fk_coupon_status_id, discount_value, maximum_discount_amount, minimum_purchase_amount, maximum_global_usage, maximum_user_usage, usage_count, is_one_time, starts_at_utc, expires_at_utc, is_active, created_at_utc)
VALUES
(UUID_V7(), 'WELCOME10', '10% discount for new customers', (SELECT pk_discount_type_id FROM coupon_discount_types WHERE discount_type_code = 'PERCENTAGE'), (SELECT pk_status_id FROM coupon_statuses WHERE status_code = 'ACTIVE'), 10.00, 500.00, 1000.00, 1000, 1, 0, TRUE, '2026-01-01 00:00:00', '2027-01-01 00:00:00', TRUE, UTC_TIMESTAMP()),
(UUID_V7(), 'FLAT500', 'Flat ₹500 discount', (SELECT pk_discount_type_id FROM coupon_discount_types WHERE discount_type_code = 'FIXED'), (SELECT pk_status_id FROM coupon_statuses WHERE status_code = 'ACTIVE'), 500.00, NULL, 5000.00, 500, 1, 0, FALSE, '2026-01-01 00:00:00', '2027-01-01 00:00:00', TRUE, UTC_TIMESTAMP());

/*==============================================================
043. SEED DATA FOR: shipping_methods
==============================================================*/

INSERT INTO shipping_methods (pk_shipping_method_id, shipping_method_name, description, base_cost, cost_per_kg, estimated_delivery_days, is_active, created_at_utc)
VALUES
(UUID_V7(), 'Standard Delivery', 'Standard shipping service', 50.00, 10.00, 5, TRUE, '2026-01-01 09:00:00'),
(UUID_V7(), 'Express Delivery', 'Fast delivery service', 150.00, 20.00, 2, TRUE, '2026-01-01 09:05:00'),
(UUID_V7(), 'Same Day Delivery', 'Delivery on the same day', 300.00, 30.00, 1, TRUE, '2026-01-01 09:10:00');

/*==============================================================
044. SEED DATA FOR: tax_configurations
==============================================================*/

INSERT INTO tax_configurations (pk_tax_configuration_id, region_name, tax_percentage, effective_from, effective_to, is_active, created_at_utc)
VALUES
(UUID_V7(), 'DEFAULT', 18.00, '2026-01-01', NULL, TRUE, '2026-01-01 00:00:00'),
(UUID_V7(), 'TELANGANA', 18.00, '2026-01-01', NULL, TRUE, '2026-01-01 00:00:00'),
(UUID_V7(), 'ANDHRA_PRADESH', 18.00, '2026-01-01', NULL, TRUE, '2026-01-01 00:00:00');
