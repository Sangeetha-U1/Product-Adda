/*
===============================================================================
Project     : Product Adda market platform
Description :
    Master execution order reference for database setup.
===============================================================================
*/

-- ============================================================================
-- Schema Design
-- ============================================================================

000_create_database.sql

-- ============================================================================
-- Table Creation
-- ============================================================================

000_execution_order.sql

001_create_roles.sql
002_create_order_statuses.sql
003_create_payment_statuses.sql
004_create_review_statuses.sql
005_create_payment_gateways.sql
006_create_address_types.sql
007_create_inventory_transaction_types.sql
008_create_notification_types.sql
009_create_notification_channels.sql
010_create_report_types.sql

011_create_users.sql
012_create_user_roles.sql
013_create_vendors.sql
014_create_vendor_bank_details.sql
015_create_addresses.sql

016_create_brands.sql
017_create_categories.sql
018_create_products.sql
019_create_product_images.sql

020_create_inventory.sql
021_create_inventory_transactions.sql

022_create_carts.sql
023_create_cart_items.sql
024_create_wishlists.sql
025_create_wishlist_items.sql

026_create_orders.sql
027_create_order_items.sql
028_create_payments.sql

029_create_reviews.sql

030_create_email_verification_tokens.sql
031_create_refresh_tokens.sql
032_create_password_reset_tokens.sql

033_create_notifications.sql

034_create_reports.sql

/*==============================================================
  Create terminal execution order
==============================================================*/

/*

Note:

1. use mysql username

eg: root

2. user mysql password

eg: YOUR__MYSQL_PASSWORD

create in user scope

```powershell
[System.Environment]::SetEnvironmentVariable("MYSQLROOTPASS", "YOUR__MYSQL_PASSWORD", "User")
```

3. provide proper path

eg: filepath: C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\

4. user at end `*> $null' to not print in terminal.

```powershell
$ScriptFolder = "C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts"

mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\001_create_roles.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\002_create_order_statuses.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\003_create_payment_statuses.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\004_create_review_statuses.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\005_create_payment_gateways.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\006_create_address_types.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\007_create_inventory_transaction_types.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\008_create_notification_types.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\009_create_notification_channels.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\010_create_report_types.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\011_create_users.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\012_create_user_roles.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\013_create_vendors.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\014_create_vendor_bank_details.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\015_create_addresses.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\016_create_brands.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\017_create_categories.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\018_create_products.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\019_create_product_images.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\020_create_inventory.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\021_create_inventory_transactions.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\022_create_carts.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\023_create_cart_items.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\024_create_wishlists.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\025_create_wishlist_items.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\026_create_orders.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\027_create_order_items.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\028_create_payments.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\029_create_reviews.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\030_create_email_verification_tokens.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\031_create_refresh_tokens.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\032_create_password_reset_tokens.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\033_create_notifications.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\scripts\034_create_reports.sql"

```
*/

/*==============================================================
  seed data
==============================================================*/

/*
```powershell
$ScriptFolder = "C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\seeddata"

mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\seeddata\01_category_lookup_tables_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\seeddata\02_category_core_master_tables_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\seeddata\03_category_product_domain_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\seeddata\04_category_inventory_domain_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\seeddata\05_category_cart_wishlist_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\seeddata\06_category_order_payment_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\seeddata\07_category_review_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\seeddata\08_category_auth_security_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\seeddata\09_category_notification_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source C:\Users\ADMIN\Downloads\vs-code\product_adda\product_adda_db\seeddata\10_category_reporting_seed_data.sql"
```
*/

/*==============================================================
  WIPE EXISTING DATA (Reverse FK Dependency Order)
==============================================================*/
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `reports`;
TRUNCATE TABLE `notifications`;
TRUNCATE TABLE `password_reset_tokens`;
TRUNCATE TABLE `refresh_tokens`;
TRUNCATE TABLE `email_verification_tokens`;
TRUNCATE TABLE `reviews`;
TRUNCATE TABLE `payments`;
TRUNCATE TABLE `order_items`;
TRUNCATE TABLE `orders`;
TRUNCATE TABLE `wishlist_items`;
TRUNCATE TABLE `wishlists`;
TRUNCATE TABLE `cart_items`;
TRUNCATE TABLE `carts`;
TRUNCATE TABLE `inventory_transactions`;
TRUNCATE TABLE `inventory`;
TRUNCATE TABLE `product_images`;
TRUNCATE TABLE `products`;
TRUNCATE TABLE `categories`;
TRUNCATE TABLE `brands`;
TRUNCATE TABLE `addresses`;
TRUNCATE TABLE `vendor_bank_details`;
TRUNCATE TABLE `vendors`;
TRUNCATE TABLE `user_roles`;
TRUNCATE TABLE `users`;
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
  DROP ALL TABLES (Reverse FK Dependency Order)
==============================================================*/
USE product_adda_db;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Dependent / Logging Tables
DROP TABLE IF EXISTS `reports`;
DROP TABLE IF EXISTS `notifications`;

-- 2. Token Security Tables
DROP TABLE IF EXISTS `password_reset_tokens`;
DROP TABLE IF EXISTS `refresh_tokens`;
DROP TABLE IF EXISTS `email_verification_tokens`;

-- 3. Order / Transactional Tables
DROP TABLE IF EXISTS `reviews`;
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;

-- 4. Cart / Wishlist Tables
DROP TABLE IF EXISTS `wishlist_items`;
DROP TABLE IF EXISTS `wishlists`;
DROP TABLE IF EXISTS `cart_items`;
DROP TABLE IF EXISTS `carts`;

-- 5. Inventory Tables
DROP TABLE IF EXISTS `inventory_transactions`;
DROP TABLE IF EXISTS `inventory`;

-- 6. Product Tables
DROP TABLE IF EXISTS `product_images`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `brands`;

-- 7. User / Vendor Core Entities
DROP TABLE IF EXISTS `addresses`;
DROP TABLE IF EXISTS `vendor_bank_details`;
DROP TABLE IF EXISTS `vendors`;
DROP TABLE IF EXISTS `user_roles`;
DROP TABLE IF EXISTS `users`;

-- 8. Master Lookup Tables
DROP TABLE IF EXISTS `report_types`;
DROP TABLE IF EXISTS `notification_channels`;
DROP TABLE IF EXISTS `notification_types`;
DROP TABLE IF EXISTS `inventory_transaction_types`;
DROP TABLE IF EXISTS `address_types`;
DROP TABLE IF EXISTS `payment_gateways`;
DROP TABLE IF EXISTS `review_statuses`;
DROP TABLE IF EXISTS `payment_statuses`;
DROP TABLE IF EXISTS `order_statuses`;
DROP TABLE IF EXISTS `roles`;

SET FOREIGN_KEY_CHECKS = 1;