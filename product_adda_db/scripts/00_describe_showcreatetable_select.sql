/*
===============================================================================
Project     : Describe and show create table and select
Description :
    Describe and show create table and select
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Describe
-- ============================================================================

DESCRIBE categories;
DESCRIBE order_items;
DESCRIBE order_statuses;
DESCRIBE orders;
DESCRIBE payment_statuses;
DESCRIBE payments;
DESCRIBE products;
DESCRIBE reviews;
DESCRIBE users;
DESCRIBE roles;
DESCRIBE user_roles;
DESCRIBE vendors;

-- ============================================================================
-- Show create table
-- ============================================================================

SHOW CREATE TABLE categories;
SHOW CREATE TABLE order_items;
SHOW CREATE TABLE order_statuses;
SHOW CREATE TABLE orders;
SHOW CREATE TABLE payment_statuses;
SHOW CREATE TABLE payments;
SHOW CREATE TABLE products;
SHOW CREATE TABLE reviews;
SHOW CREATE TABLE users;
SHOW CREATE TABLE roles;
SHOW CREATE TABLE user_roles;
SHOW CREATE TABLE vendors;

-- ============================================================================
-- Select
-- ============================================================================

SELECT * FROM categories;
SELECT * FROM order_items;
SELECT * FROM order_statuses;
SELECT * FROM orders;
SELECT * FROM payment_statuses;
SELECT * FROM payments;
SELECT * FROM products;
SELECT * FROM reviews;
SELECT * FROM users;
SELECT * FROM roles;
SELECT * FROM user_roles;
SELECT * FROM vendors;

