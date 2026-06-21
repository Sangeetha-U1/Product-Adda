/*
===============================================================================
Project     : Product adaa
Description :
    Master execution order reference for database setup.
===============================================================================
*/

-- ============================================================================
-- Table Creation
-- ============================================================================

-- 00_create_drop_execution order.sql
-- 001_create_users.sql
-- 002_create_vendors.sql
-- 003_create_categories.sql
-- 004_create_products.sql
-- 005_create_order_statuses.sql
-- 006_create_payment_statuses.sql
-- 007_create_orders.sql
-- 008_create_order_items.sql
-- 009_create_payments.sql
-- 010_create_reviews.sql
-- 011_create_roles.sql
-- 012_create_user_roles.sql
-- 013_create_email_verification_tokens.sql

/*==============================================================
  WIPE EXISTING DATA (Reverse FK Dependency Order)
==============================================================*/
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE reviews;
TRUNCATE TABLE payments;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE order_statuses;  
TRUNCATE TABLE payment_statuses; 
TRUNCATE TABLE products;
TRUNCATE TABLE vendors;
TRUNCATE TABLE categories;
TRUNCATE TABLE users;
TRUNCATE TABLE roles;
TRUNCATE TABLE user_roles;
TRUNCATE TABLE email_verification_tokens;
TRUNCATE TABLE refresh_tokens;
TRUNCATE TABLE password_reset_tokens
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- Drop table
-- ============================================================================

USE product_adda_db;

-- ============================================================================
-- Disable foreign key checks to ensure a smooth drop process (Safe Practice)
-- ============================================================================
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- Drop Transactional / Dependent Tables First
-- ============================================================================
DROP TABLE IF EXISTS `reviews`;
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;

-- ============================================================================
-- Drop Master / Lookup Tables Next
-- ============================================================================
DROP TABLE IF EXISTS `payment_statuses`;
DROP TABLE IF EXISTS `order_statuses`;
DROP TABLE IF EXISTS `roles`;

-- ============================================================================
-- Drop Core / Independent Tables Last
-- ============================================================================
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `vendors`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `user_roles`;
DROP TABLE IF EXISTS `email_verification_tokens`;
DROP TABLE IF EXISTS `refresh_tokens`;
DROP TABLE IF EXISTS `password_reset_tokens`;

-- ============================================================================
-- Re-enable foreign key checks
-- ============================================================================
SET FOREIGN_KEY_CHECKS = 1;
