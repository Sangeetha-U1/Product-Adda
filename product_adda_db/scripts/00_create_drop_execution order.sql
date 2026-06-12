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

-- 001_create_users.sql
-- 002_create_vendors.sql
-- 003_create_categories.sql
-- 004_create_products.sql
-- 005_create_orders.sql
-- 006_create_order_items.sql
-- 007_create_payments.sql
-- 008_create_reviews.sql

-- ============================================================================
-- Drop table
-- ============================================================================

DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS vendors;
DROP TABLE IF EXISTS users;
