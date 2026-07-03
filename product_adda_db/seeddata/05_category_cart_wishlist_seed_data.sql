USE product_adda_db;

/*==============================================================
000. TRUNCATE
==============================================================*/

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `wishlist_items`;
TRUNCATE TABLE `wishlists`;
TRUNCATE TABLE `cart_items`;
TRUNCATE TABLE `carts`;

SET FOREIGN_KEY_CHECKS = 1;

/*===============================================================================
022. SEED DATA FOR: carts
===============================================================================*/

INSERT INTO carts (pk_cart_id, fk_user_id, fk_cart_status_id, fk_coupon_id, is_active, created_at_utc, expires_at_utc) VALUES
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), (SELECT pk_status_id FROM cart_statuses WHERE status_code = 'ACTIVE'), NULL, TRUE, '2025-04-01 10:00:00', '2025-06-30 10:00:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com'), (SELECT pk_status_id FROM cart_statuses WHERE status_code = 'ACTIVE'), NULL, TRUE, '2025-04-02 11:00:00', '2025-07-01 11:00:00');

/*===============================================================================
023. SEED DATA FOR: cart_items
===============================================================================*/

INSERT INTO cart_items (pk_cart_item_id, fk_cart_id, fk_product_id, quantity, price_at_add, added_at_utc, is_saved_for_later, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_cart_id FROM carts c JOIN users u ON c.fk_user_id = u.pk_user_id WHERE u.email = 'rahul.verma@gmail.com'), (SELECT pk_product_id FROM products WHERE sku = 'MOB-SAMSUNG-S24-001'), 2, (SELECT COALESCE(discount_price, price) FROM products WHERE sku = 'MOB-SAMSUNG-S24-001'), '2025-04-01 10:15:00', FALSE, '2025-04-01 10:15:00'),
(UUID_V7(), (SELECT pk_cart_id FROM carts c JOIN users u ON c.fk_user_id = u.pk_user_id WHERE u.email = 'pooja.singh@yahoo.com'), (SELECT pk_product_id FROM products WHERE sku = 'LAP-DELL-XPS15-001'), 1, (SELECT COALESCE(discount_price, price) FROM products WHERE sku = 'LAP-DELL-XPS15-001'), '2025-04-02 11:15:00', TRUE, '2025-04-02 11:15:00');

/*===============================================================================
024. SEED DATA FOR: wishlists
===============================================================================*/

INSERT INTO wishlists (pk_wishlist_id, fk_user_id, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), '2025-04-05 09:00:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com'), '2025-04-05 09:30:00');

/*===============================================================================
025. SEED DATA FOR: wishlist_items
===============================================================================*/

INSERT INTO wishlist_items (pk_wishlist_item_id, fk_wishlist_id, fk_product_id, price_at_add, is_active, is_deleted, created_at_utc, expires_at_utc) VALUES
(UUID_V7(), (SELECT pk_wishlist_id FROM wishlists w JOIN users u ON w.fk_user_id = u.pk_user_id WHERE u.email = 'rahul.verma@gmail.com'), (SELECT pk_product_id FROM products WHERE sku = 'LAP-DELL-XPS15-001'), (SELECT COALESCE(discount_price, price) FROM products WHERE sku = 'LAP-DELL-XPS15-001'), TRUE, FALSE, '2025-04-05 09:15:00', '2025-07-04 09:15:00'),
(UUID_V7(), (SELECT pk_wishlist_id FROM wishlists w JOIN users u ON w.fk_user_id = u.pk_user_id WHERE u.email = 'pooja.singh@yahoo.com'), (SELECT pk_product_id FROM products WHERE sku = 'MOB-SAMSUNG-S24-001'), (SELECT COALESCE(discount_price, price) FROM products WHERE sku = 'MOB-SAMSUNG-S24-001'), TRUE, FALSE, '2025-04-05 09:45:00', '2025-07-04 09:45:00');
