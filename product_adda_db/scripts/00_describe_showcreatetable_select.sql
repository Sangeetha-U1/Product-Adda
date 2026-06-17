/*
===============================================================================
Project     : Product Adda Market Platform
Description :
    Grouped verification commands for all 34 tables.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- 1. DESCRIBE ALL TABLES
-- ============================================================================

DESCRIBE roles;
DESCRIBE order_statuses;
DESCRIBE payment_statuses;
DESCRIBE review_statuses;
DESCRIBE payment_gateways;
DESCRIBE address_types;
DESCRIBE inventory_transaction_types;
DESCRIBE notification_types;
DESCRIBE notification_channels;
DESCRIBE report_types;
DESCRIBE users;
DESCRIBE user_roles;
DESCRIBE vendors;
DESCRIBE vendor_bank_details;
DESCRIBE addresses;
DESCRIBE brands;
DESCRIBE categories;
DESCRIBE products;
DESCRIBE product_images;
DESCRIBE inventory;
DESCRIBE inventory_transactions;
DESCRIBE carts;
DESCRIBE cart_items;
DESCRIBE wishlists;
DESCRIBE wishlist_items;
DESCRIBE orders;
DESCRIBE order_items;
DESCRIBE payments;
DESCRIBE reviews;
DESCRIBE email_verification_tokens;
DESCRIBE refresh_tokens;
DESCRIBE password_reset_tokens;
DESCRIBE notifications;
DESCRIBE reports;


-- ============================================================================
-- 2. SHOW CREATE TABLE FOR ALL TABLES
-- ============================================================================

SHOW CREATE TABLE roles;
SHOW CREATE TABLE order_statuses;
SHOW CREATE TABLE payment_statuses;
SHOW CREATE TABLE review_statuses;
SHOW CREATE TABLE payment_gateways;
SHOW CREATE TABLE address_types;
SHOW CREATE TABLE inventory_transaction_types;
SHOW CREATE TABLE notification_types;
SHOW CREATE TABLE notification_channels;
SHOW CREATE TABLE report_types;
SHOW CREATE TABLE users;
SHOW CREATE TABLE user_roles;
SHOW CREATE TABLE vendors;
SHOW CREATE TABLE vendor_bank_details;
SHOW CREATE TABLE addresses;
SHOW CREATE TABLE brands;
SHOW CREATE TABLE categories;
SHOW CREATE TABLE products;
SHOW CREATE TABLE product_images;
SHOW CREATE TABLE inventory;
SHOW CREATE TABLE inventory_transactions;
SHOW CREATE TABLE carts;
SHOW CREATE TABLE cart_items;
SHOW CREATE TABLE wishlists;
SHOW CREATE TABLE wishlist_items;
SHOW CREATE TABLE orders;
SHOW CREATE TABLE order_items;
SHOW CREATE TABLE payments;
SHOW CREATE TABLE reviews;
SHOW CREATE TABLE email_verification_tokens;
SHOW CREATE TABLE refresh_tokens;
SHOW CREATE TABLE password_reset_tokens;
SHOW CREATE TABLE notifications;
SHOW CREATE TABLE reports;


-- ============================================================================
-- 3. SELECT FROM ALL TABLES
-- ============================================================================

SELECT * FROM roles;
SELECT * FROM order_statuses;
SELECT * FROM payment_statuses;
SELECT * FROM review_statuses;
SELECT * FROM payment_gateways;
SELECT * FROM address_types;
SELECT * FROM inventory_transaction_types;
SELECT * FROM notification_types;
SELECT * FROM notification_channels;
SELECT * FROM report_types;

SELECT * FROM users;
SELECT * FROM user_roles;
SELECT * FROM vendors;
SELECT * FROM vendor_bank_details;
SELECT * FROM addresses;
SELECT * FROM brands;
SELECT * FROM categories;
SELECT * FROM products;
SELECT * FROM product_images;
SELECT * FROM inventory;
SELECT * FROM inventory_transactions;
SELECT * FROM carts;
SELECT * FROM cart_items;
SELECT * FROM wishlists;
SELECT * FROM wishlist_items;
SELECT * FROM orders;
SELECT * FROM order_items;
SELECT * FROM payments;
SELECT * FROM reviews;
SELECT * FROM email_verification_tokens;
SELECT * FROM refresh_tokens;
SELECT * FROM password_reset_tokens;
SELECT * FROM notifications;
SELECT * FROM reports;
