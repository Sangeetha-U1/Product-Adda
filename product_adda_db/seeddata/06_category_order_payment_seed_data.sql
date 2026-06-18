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
-- Initial Orders
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'CONFIRMED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com') LIMIT 1), 131998.00, TRUE, '2025-05-01 14:00:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'DELIVERED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com') LIMIT 1), 1999.00, TRUE, '2025-05-02 11:30:00'),

-- 1. Varun Sharma (SuperAdmin buying some office gear)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'superadmin@productadda.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PROCESSING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'superadmin@productadda.com') LIMIT 1), 45000.00, TRUE, '2025-05-21 10:00:00'),

-- 2. Amit Sharma (Admin testing a purchase)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'SHIPPED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com') LIMIT 1), 1500.00, TRUE, '2025-05-21 11:30:00'),

-- 3. Rajesh Kumar (Vendor buying sample stock - PENDING #1)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rajesh@techsolutions.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PENDING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'rajesh@techsolutions.com') LIMIT 1), 89999.00, TRUE, '2025-05-22 14:15:00'),

-- 4. Anita Desai (Vendor restocking supplies)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'anita@fashionhub.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'CONFIRMED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'anita@fashionhub.com') LIMIT 1), 12450.00, TRUE, '2025-05-22 16:00:00'),

-- 5. Varun Sharma (SuperAdmin order got cancelled)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'superadmin@productadda.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'CANCELLED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'superadmin@productadda.com') LIMIT 1), 3200.00, TRUE, '2025-05-23 09:45:00'),

-- 6. Amit Sharma (Admin returned an item)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'RETURNED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com') LIMIT 1), 750.00, TRUE, '2025-05-23 13:20:00'),

-- 7. Rajesh Kumar (Vendor order completely refunded)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rajesh@techsolutions.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'REFUNDED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'rajesh@techsolutions.com') LIMIT 1), 54000.00, TRUE, '2025-05-24 11:10:00'),

-- 8. Anita Desai (Fashion hub order delivered)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'anita@fashionhub.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'DELIVERED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'anita@fashionhub.com') LIMIT 1), 6800.00, TRUE, '2025-05-24 17:05:00'),

-- 9. Vikram Malhotra (Another customer order processing)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'vikram.m@gmail.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PROCESSING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'vikram.m@gmail.com') LIMIT 1), 999.00, TRUE, '2025-05-25 08:30:00'),

-- 10. Rahul Verma (Customer order shipped out)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'SHIPPED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com') LIMIT 1), 4200.00, TRUE, '2025-05-25 12:00:00'),

-- 11. Pooja Singh (Customer order cancelled midway)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'CANCELLED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com') LIMIT 1), 1350.00, TRUE, '2025-05-26 15:30:00'),

-- 12. Vikram Malhotra (Customer order successfully delivered)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'vikram.m@gmail.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'DELIVERED'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'vikram.m@gmail.com') LIMIT 1), 27500.00, TRUE, '2025-05-26 19:10:00'),

-- 13. Varun Sharma (SuperAdmin back-office purchase - PENDING #2)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'superadmin@productadda.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PENDING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'superadmin@productadda.com') LIMIT 1), 11500.00, TRUE, '2025-05-27 09:00:00'),

-- 14. Amit Sharma (Admin sandbox checkout - PENDING #3)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PENDING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com') LIMIT 1), 2450.00, TRUE, '2025-05-27 10:30:00'),

-- 15. Anita Desai (Vendor textile order - PENDING #4)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'anita@fashionhub.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PENDING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'anita@fashionhub.com') LIMIT 1), 38200.00, TRUE, '2025-05-27 11:15:00'),

-- 16. Rahul Verma (Customer phone accessory cart - PENDING #5)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PENDING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com') LIMIT 1), 1899.00, TRUE, '2025-05-27 14:00:00'),

-- 17. Pooja Singh (Customer home decor cart - PENDING #6)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PENDING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com') LIMIT 1), 4750.00, TRUE, '2025-05-27 15:45:00'),

-- 18. Vikram Malhotra (Customer electronics upgrade - PENDING #7)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'vikram.m@gmail.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PENDING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'vikram.m@gmail.com') LIMIT 1), 63900.00, TRUE, '2025-05-27 16:20:00'),

-- 19. Rahul Verma (Customer quick reorder - PENDING #8)
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), (SELECT pk_status_id FROM order_statuses WHERE status_name = 'PENDING'), (SELECT pk_address_id FROM addresses WHERE fk_user_id = (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com') LIMIT 1), 850.00, TRUE, '2025-05-27 18:10:00');

/*
===============================================================================
027. SEED DATA FOR: order_items
===============================================================================
*/

INSERT INTO order_items (pk_order_item_id, fk_order_id, fk_product_id, product_name_snapshot, quantity, unit_price, is_active, created_at_utc) VALUES
-- Initial Orders
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 131998.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Samsung Galaxy S24 Ultra', 1, 124999.00, TRUE, '2025-05-01 14:05:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 1999.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1 OFFSET 1), 'Nike Sports T-Shirt', 1, 1499.00, TRUE, '2025-05-02 11:35:00'),
-- 1. Varun Sharma (PROCESSING)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 45000.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Office Desk & Ergonomic Chair Combo', 1, 45000.00, TRUE, '2025-05-21 10:05:00'),
-- 2. Amit Sharma (SHIPPED)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 1500.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Wireless Testing Mouse', 1, 1500.00, TRUE, '2025-05-21 11:35:00'),
-- 3. Rajesh Kumar (PENDING #1)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 89999.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Wholesale Component Batch', 1, 89999.00, TRUE, '2025-05-22 14:20:00'),
-- 4. Anita Desai (CONFIRMED)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 12450.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Boutique Fabric Assortment', 1, 12450.00, TRUE, '2025-05-22 16:05:00'),
-- 5. Varun Sharma (CANCELLED)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 3200.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Misordered Standing Frame', 1, 3200.00, TRUE, '2025-05-23 09:50:00'),
-- 6. Amit Sharma (RETURNED)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 750.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Faulty USB-C Hub Extension', 1, 750.00, TRUE, '2025-05-23 13:25:00'),
-- 7. Rajesh Kumar (REFUNDED)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 54000.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Bulk Monitor Shipment', 1, 54000.00, TRUE, '2025-05-24 11:15:00'),
-- 8. Anita Desai (DELIVERED)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 6800.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Designer Dress Mannequins', 1, 6800.00, TRUE, '2025-05-24 17:10:00'),
-- 9. Vikram Malhotra (PROCESSING)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 999.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Premium Tempered Glass Protector', 1, 999.00, TRUE, '2025-05-25 08:35:00'),
-- 10. Rahul Verma (SHIPPED)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 4200.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Leather Casual Jacket', 1, 4200.00, TRUE, '2025-05-25 12:05:00'),
-- 11. Pooja Singh (CANCELLED)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 1350.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Handmade Ceramic Flower Vase', 1, 1350.00, TRUE, '2025-05-26 15:35:00'),
-- 12. Vikram Malhotra (DELIVERED)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 27500.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Mechanical Gaming Keyboard Setup', 1, 27500.00, TRUE, '2025-05-26 19:15:00'),
-- 13. Varun Sharma (PENDING #2)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 11500.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Server Rack Management Shelf', 1, 11500.00, TRUE, '2025-05-27 09:05:00'),
-- 14. Amit Sharma (PENDING #3)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 2450.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Braided Cat6 Ethernet Reel', 1, 2450.00, TRUE, '2025-05-27 10:35:00'),
-- 15. Anita Desai (PENDING #4)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 38200.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Industrial Sewing Stitcher Pack', 1, 38200.00, TRUE, '2025-05-27 11:20:00'),
-- 16. Rahul Verma (PENDING #5)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 1899.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'MagSafe Powerbank Pack', 1, 1899.00, TRUE, '2025-05-27 14:05:00'),
-- 17. Pooja Singh (PENDING #6)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 4750.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Bohemian Living Room Rug', 1, 4750.00, TRUE, '2025-05-27 15:50:00'),
-- 18. Vikram Malhotra (PENDING #7)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 63900.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), '4K Ultra-Wide Desk Display', 1, 63900.00, TRUE, '2025-05-27 16:25:00'),
-- 19. Rahul Verma (PENDING #8)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 850.00 LIMIT 1), (SELECT pk_product_id FROM products LIMIT 1), 'Braided Multi-USB Cable Set', 1, 850.00, TRUE, '2025-05-27 18:15:00');

/*
===============================================================================
028. SEED DATA FOR: payments
===============================================================================
*/

INSERT INTO payments (pk_payment_id, fk_order_id, fk_status_id, fk_gateway_id, payment_method, gateway_transaction_id, gateway_order_id, gateway_signature, amount_paid, paid_at_utc, is_active, created_at_utc) VALUES
-- Initial Orders
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 131998.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways WHERE gateway_name LIKE '%RAZORPAY%' LIMIT 1), 'ONLINE', 'pay_test_razorpay_001', 'order_test_razorpay_001', 'signature_test_001', 131998.00, '2025-05-01 14:10:00', TRUE, '2025-05-01 14:10:00'),
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 1999.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways WHERE gateway_name LIKE '%STRIPE%' LIMIT 1), 'CARD', 'txn_stripe_test_001', 'order_stripe_test_001', 'signature_test_002', 1999.00, '2025-05-02 11:40:00', TRUE, '2025-05-02 11:40:00'),
-- 1. Varun Sharma (PROCESSING -> Payment Successful)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 45000.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'ONLINE', 'pay_v7_001', 'go_v7_001', 'sig_v7_001', 45000.00, '2025-05-21 10:02:00', TRUE, '2025-05-21 10:02:00'),
-- 2. Amit Sharma (SHIPPED -> Payment Successful)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 1500.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'CARD', 'pay_v7_002', 'go_v7_002', 'sig_v7_002', 1500.00, '2025-05-21 11:32:00', TRUE, '2025-05-21 11:32:00'),
-- 4. Anita Desai (CONFIRMED -> Payment Successful)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 12450.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'ONLINE', 'pay_v7_004', 'go_v7_004', 'sig_v7_004', 12450.00, '2025-05-22 16:02:00', TRUE, '2025-05-22 16:02:00'),
-- 5. Varun Sharma (CANCELLED -> Payment Failed / Aborted)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 3200.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name LIKE '%FAIL%' OR status_name LIKE '%CANCEL%' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'ONLINE', 'pay_v7_005', 'go_v7_005', NULL, 0.00, NULL, TRUE, '2025-05-23 09:47:00'),
-- 6. Amit Sharma (RETURNED -> Initial Success, Now Flagged for Refund processing)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 750.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'CARD', 'pay_v7_006', 'go_v7_006', 'sig_v7_006', 750.00, '2025-05-23 13:22:00', TRUE, '2025-05-23 13:22:00'),
-- 7. Rajesh Kumar (REFUNDED -> Reversed payment reference)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 54000.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name LIKE '%REFUND%' OR status_name LIKE '%REVERSED%' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'ONLINE', 'pay_v7_007', 'go_v7_007', 'sig_v7_007', 54000.00, '2025-05-24 11:12:00', TRUE, '2025-05-24 11:12:00'),
-- 8. Anita Desai (DELIVERED -> Payment Fully Cleared)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 6800.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'ONLINE', 'pay_v7_008', 'go_v7_008', 'sig_v7_008', 6800.00, '2025-05-24 17:07:00', TRUE, '2025-05-24 17:07:00'),
-- 9. Vikram Malhotra (PROCESSING -> Cleared Funds)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 999.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'CARD', 'pay_v7_009', 'go_v7_009', 'sig_v7_009', 999.00, '2025-05-25 08:32:00', TRUE, '2025-05-25 08:32:00'),
-- 10. Rahul Verma (SHIPPED -> Fully Captured)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 4200.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'ONLINE', 'pay_v7_010', 'go_v7_010', 'sig_v7_010', 4200.00, '2025-05-25 12:02:00', TRUE, '2025-05-25 12:02:00'),
-- 11. Pooja Singh (CANCELLED -> Abandoned Gateway Session)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 1350.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name LIKE '%FAIL%' OR status_name LIKE '%CANCEL%' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'ONLINE', 'pay_v7_011', 'go_v7_011', NULL, 0.00, NULL, TRUE, '2025-05-26 15:32:00'),
-- 12. Vikram Malhotra (DELIVERED -> Payment Completed Natively)
(UUID_V7(), (SELECT pk_order_id FROM orders WHERE total_amount = 27500.00 LIMIT 1), (SELECT pk_status_id FROM payment_statuses WHERE status_name = 'SUCCESS' LIMIT 1), (SELECT pk_gateway_id FROM payment_gateways LIMIT 1), 'CARD', 'pay_v7_012', 'go_v7_012', 'sig_v7_012', 27500.00, '2025-05-26 19:12:00', TRUE, '2025-05-26 19:12:00');
