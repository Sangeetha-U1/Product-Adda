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
035_create_product_status_lookup.sql
036_create_cart_statuses.sql
037_create_coupon_statuses.sql
038_create_coupon_discount_types.sql

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
039_create_inventory_reservations.sql

022_create_carts.sql
023_create_cart_items.sql
024_create_wishlists.sql
025_create_wishlist_items.sql
040_create_wishlist_price_history.sql

026_create_orders.sql
027_create_order_items.sql
028_create_payments.sql
041_create_coupons.sql
042_create_coupon_usage_history.sql
043_create_shipping_methods.sql
044_create_tax_configurations.sql
045_create_delivery_partners.sql
046_create_delivery_assignments.sql
047_create_invoices.sql
048_create_invoice_line_items.sql

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

eg: filepath: folderpath

4. user at end `*> $null' to not print in terminal.

```powershell
$ScriptFolder = "folderpath"

mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\001_create_roles.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\002_create_order_statuses.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\003_create_payment_statuses.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\004_create_review_statuses.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\005_create_payment_gateways.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\006_create_address_types.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\007_create_inventory_transaction_types.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\008_create_notification_types.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\009_create_notification_channels.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\010_create_report_types.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\035_create_product_status_lookup.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\036_create_cart_statuses.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\037_create_coupon_statuses.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\038_create_coupon_discount_types.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\011_create_users.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\012_create_user_roles.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\013_create_vendors.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\014_create_vendor_bank_details.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\015_create_addresses.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\016_create_brands.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\017_create_categories.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\018_create_products.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\019_create_product_images.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\020_create_inventory.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\021_create_inventory_transactions.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\039_create_inventory_reservations.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\022_create_carts.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\023_create_cart_items.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\024_create_wishlists.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\025_create_wishlist_items.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\040_create_wishlist_price_history.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\026_create_orders.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\027_create_order_items.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\028_create_payments.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\041_create_coupons.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\042_create_coupon_usage_history.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\043_create_shipping_methods.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\044_create_tax_configurations.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\045_create_delivery_partners.sql
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\046_create_delivery_assignments.sql
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\047_create_invoices.sql
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\048_create_invoice_line_items.sql
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\029_create_reviews.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\030_create_email_verification_tokens.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\031_create_refresh_tokens.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\032_create_password_reset_tokens.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\033_create_notifications.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\034_create_reports.sql"

```
*/

/*==============================================================
  seed data
==============================================================*/

/*
```powershell
$ScriptFolder = "folderpath"

mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\01_category_lookup_tables_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\02_category_core_master_tables_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\03_category_product_domain_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\04_category_inventory_domain_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\05_category_cart_wishlist_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\06_category_order_payment_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\07_category_review_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\08_category_auth_security_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\09_category_notification_seed_data.sql"
mysql -u root "-p$env:MYSQLROOTPASS" product_adda_db -e "source $ScriptFolder\10_category_reporting_seed_data.sql"
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
TRUNCATE TABLE `invoice_line_items`;
TRUNCATE TABLE `invoices`;
TRUNCATE TABLE `delivery_assignments`;
TRUNCATE TABLE `delivery_partners`;
TRUNCATE TABLE `tax_configurations`;
TRUNCATE TABLE `shipping_methods`;
TRUNCATE TABLE `coupons`;
TRUNCATE TABLE `payments`;
TRUNCATE TABLE `order_items`;
TRUNCATE TABLE `orders`;
TRUNCATE TABLE `wishlist_price_history`;
TRUNCATE TABLE `wishlist_items`;
TRUNCATE TABLE `wishlists`;
TRUNCATE TABLE `cart_items`;
TRUNCATE TABLE `carts`;
TRUNCATE TABLE `inventory_reservations`;
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
TRUNCATE TABLE `coupon_discount_types`;
TRUNCATE TABLE `coupon_statuses`;
TRUNCATE TABLE `cart_statuses`;
TRUNCATE TABLE `product_status_lookup`;
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

DROP TABLE IF EXISTS `reviews`;

-- 3. Order / Transactional Tables
DROP TABLE IF EXISTS `delivery_partners`;
DROP TABLE IF EXISTS `delivery_assignments`;
DROP TABLE IF EXISTS `invoices`;
DROP TABLE IF EXISTS `invoice_line_items`;
DROP TABLE IF EXISTS `tax_configurations`;
DROP TABLE IF EXISTS `shipping_methods`;
DROP TABLE IF EXISTS `coupons`;
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;

-- 4. Cart / Wishlist Tables
DROP TABLE IF EXISTS `wishlist_price_history`;
DROP TABLE IF EXISTS `wishlist_items`;
DROP TABLE IF EXISTS `wishlists`;
DROP TABLE IF EXISTS `cart_items`;
DROP TABLE IF EXISTS `carts`;

-- 5. Inventory Tables
DROP TABLE IF EXISTS `inventory_reservations`;
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
DROP TABLE IF EXISTS `coupon_discount_types`;
DROP TABLE IF EXISTS `coupon_statuses`;
DROP TABLE IF EXISTS `cart_statuses`;
DROP TABLE IF EXISTS `product_status_lookup`;
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