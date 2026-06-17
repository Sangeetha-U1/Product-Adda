USE product_adda_db;

/*==============================================================
000. TRUNCATE
==============================================================*/

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `reviews`;

SET FOREIGN_KEY_CHECKS = 1;

/*
===============================================================================
029. SEED DATA FOR: reviews
===============================================================================
*/

INSERT INTO reviews (pk_review_id, fk_product_id, fk_user_id, fk_status_id, rating, review_title, review_text, created_at_utc, is_active) VALUES
(UUID_V7(), (SELECT pk_product_id FROM products WHERE sku = 'MOB-SAMSUNG-S24-001'), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), (SELECT pk_status_id FROM review_statuses WHERE status_name = 'APPROVED'), 5, 'Excellent Smartphone', 'Amazing performance, camera quality and battery life.', '2025-06-15 11:20:00', TRUE),
(UUID_V7(), (SELECT pk_product_id FROM products WHERE sku = 'LAP-DELL-XPS15-001'), (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com'), (SELECT pk_status_id FROM review_statuses WHERE status_name = 'PENDING'), 4, 'Great Laptop', 'Premium build quality and smooth performance.', '2025-06-22 16:45:00', TRUE),
(UUID_V7(), (SELECT pk_product_id FROM products WHERE sku = 'MEN-NIKE-TSHIRT-001'), (SELECT pk_user_id FROM users WHERE email = 'vikram.m@gmail.com'), (SELECT pk_status_id FROM review_statuses WHERE status_name = 'APPROVED'), 5, 'Comfortable Product', 'Good quality fabric and comfortable fitting.', '2025-07-01 09:30:00', TRUE);
