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
DESCRIBE product_status_lookup;
DESCRIBE cart_statuses;
DESCRIBE coupon_statuses;
DESCRIBE coupon_discount_types;

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
DESCRIBE inventory_reservations;

DESCRIBE carts;
DESCRIBE cart_items;
DESCRIBE wishlists;
DESCRIBE wishlist_items;
DESCRIBE wishlist_price_history;

DESCRIBE orders;
DESCRIBE order_items;
DESCRIBE payments;
DESCRIBE coupons;
DESCRIBE coupon_usage_history;
DESCRIBE shipping_methods;
DESCRIBE tax_configurations;

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
SHOW CREATE TABLE product_status_lookup;
SHOW CREATE TABLE cart_statuses;
SHOW CREATE TABLE coupon_statuses;
SHOW CREATE TABLE coupon_discount_types;

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
SHOW CREATE TABLE inventory_reservations;

SHOW CREATE TABLE carts;
SHOW CREATE TABLE cart_items;
SHOW CREATE TABLE wishlists;
SHOW CREATE TABLE wishlist_items;
SHOW CREATE TABLE wishlist_price_history;

SHOW CREATE TABLE orders;
SHOW CREATE TABLE order_items;
SHOW CREATE TABLE payments;
SHOW CREATE TABLE coupons;
SHOW CREATE TABLE coupon_usage_history;
SHOW CREATE TABLE shipping_methods;
SHOW CREATE TABLE tax_configurations;

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
SELECT * FROM product_status_lookup;
SELECT * FROM cart_statuses;
SELECT * FROM coupon_statuses;
SELECT * FROM coupon_discount_types;

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
SELECT * FROM inventory_reservations;

SELECT * FROM carts;
SELECT * FROM cart_items;
SELECT * FROM wishlists;
SELECT * FROM wishlist_items;
SELECT * FROM wishlist_price_history;

SELECT * FROM orders;
SELECT * FROM order_items;
SELECT * FROM payments;
SELECT * FROM coupons;
SELECT * FROM coupon_usage_history;
SELECT * FROM shipping_methods;
SELECT * FROM tax_configurations;

SELECT * FROM reviews;

SELECT * FROM email_verification_tokens;
SELECT * FROM refresh_tokens;
SELECT * FROM password_reset_tokens;

SELECT * FROM notifications;

SELECT * FROM reports;

/*============================================================================
3. SELECT FROM ALL TABLES COMPREHENSIVE
============================================================================*/

-- ==============================================================
-- 1. roles
-- ==============================================================
SELECT
    roles.pk_role_id,
    roles.role_name,
    roles.description,
    roles.is_active,
    roles.created_at_utc,
    CONVERT_TZ(roles.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    roles.updated_at_utc,
    CONVERT_TZ(roles.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM roles;

-- ==============================================================
-- 2. order_statuses
-- ==============================================================
SELECT
    order_statuses.pk_status_id,
    order_statuses.status_name,
    order_statuses.description,
    order_statuses.is_active,
    order_statuses.created_at_utc,
    CONVERT_TZ(order_statuses.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    order_statuses.updated_at_utc,
    CONVERT_TZ(order_statuses.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM order_statuses;

-- ==============================================================
-- 3. payment_statuses
-- ==============================================================
SELECT
    payment_statuses.pk_status_id,
    payment_statuses.status_name,
    payment_statuses.description,
    payment_statuses.is_active,
    payment_statuses.created_at_utc,
    CONVERT_TZ(payment_statuses.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    payment_statuses.updated_at_utc,
    CONVERT_TZ(payment_statuses.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM payment_statuses;

-- ==============================================================
-- 4. review_statuses
-- ==============================================================
SELECT
    review_statuses.pk_status_id,
    review_statuses.status_name,
    review_statuses.description,
    review_statuses.is_active,
    review_statuses.created_at_utc,
    CONVERT_TZ(review_statuses.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    review_statuses.updated_at_utc,
    CONVERT_TZ(review_statuses.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM review_statuses;

-- ==============================================================
-- 5. payment_gateways
-- ==============================================================
SELECT
    payment_gateways.pk_gateway_id,
    payment_gateways.gateway_name,
    payment_gateways.description,
    payment_gateways.is_active,
    payment_gateways.created_at_utc,
    CONVERT_TZ(payment_gateways.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    payment_gateways.updated_at_utc,
    CONVERT_TZ(payment_gateways.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM payment_gateways;

-- ==============================================================
-- 6. address_types
-- ==============================================================
SELECT
    address_types.pk_address_type_id,
    address_types.address_type_name,
    address_types.description,
    address_types.is_active,
    address_types.created_at_utc,
    CONVERT_TZ(address_types.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    address_types.updated_at_utc,
    CONVERT_TZ(address_types.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM address_types;

-- ==============================================================
-- 7. inventory_transaction_types
-- ==============================================================
SELECT
    inventory_transaction_types.pk_transaction_type_id,
    inventory_transaction_types.transaction_type_name,
    inventory_transaction_types.description,
    inventory_transaction_types.is_active,
    inventory_transaction_types.created_at_utc,
    CONVERT_TZ(inventory_transaction_types.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    inventory_transaction_types.updated_at_utc,
    CONVERT_TZ(inventory_transaction_types.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM inventory_transaction_types;

-- ==============================================================
-- 8. notification_types
-- ==============================================================
SELECT
    notification_types.pk_notification_type_id,
    notification_types.notification_type_name,
    notification_types.description,
    notification_types.is_active,
    notification_types.created_at_utc,
    CONVERT_TZ(notification_types.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    notification_types.updated_at_utc,
    CONVERT_TZ(notification_types.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM notification_types;

-- ==============================================================
-- 9. notification_channels
-- ==============================================================
SELECT
    notification_channels.pk_channel_id,
    notification_channels.channel_name,
    notification_channels.description,
    notification_channels.is_active,
    notification_channels.created_at_utc,
    CONVERT_TZ(notification_channels.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    notification_channels.updated_at_utc,
    CONVERT_TZ(notification_channels.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM notification_channels;

-- ==============================================================
-- 10. report_types
-- ==============================================================
SELECT
    report_types.pk_report_type_id,
    report_types.report_type_name,
    report_types.description,
    report_types.is_active,
    report_types.created_at_utc,
    CONVERT_TZ(report_types.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    report_types.updated_at_utc,
    CONVERT_TZ(report_types.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM report_types;

-- ==============================================================
-- 35. product_status_lookup
-- ==============================================================
SELECT
    product_status_lookup.pk_status_id,
    product_status_lookup.status_code, 
    product_status_lookup.status_label, 
    product_status_lookup.display_order, 
    product_status_lookup.is_active, 
    product_status_lookup.created_at_utc,
    CONVERT_TZ(product_status_lookup.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    product_status_lookup.updated_at_utc,
    CONVERT_TZ(product_status_lookup.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM product_status_lookup;

-- ==============================================================
-- 036. SEED DATA FOR: cart_statuses
-- ==============================================================
SELECT
    cart_statuses.pk_status_id,
    cart_statuses.status_code, 
    cart_statuses.status_label, 
    cart_statuses.display_order, 
    cart_statuses.is_active, 
    cart_statuses.created_at_utc,
    CONVERT_TZ(cart_statuses.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    cart_statuses.updated_at_utc,
    CONVERT_TZ(cart_statuses.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM cart_statuses;

-- ==============================================================
-- 037. SEED DATA FOR: coupon_statuses
-- ==============================================================
SELECT
    coupon_statuses.pk_status_id,
    coupon_statuses.status_code, 
    coupon_statuses.status_label, 
    coupon_statuses.display_order, 
    coupon_statuses.is_active, 
    coupon_statuses.created_at_utc,
    CONVERT_TZ(coupon_statuses.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    coupon_statuses.updated_at_utc,
    CONVERT_TZ(coupon_statuses.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM coupon_statuses;

-- ==============================================================
-- 038. SEED DATA FOR: coupon_discount_types
-- ==============================================================
SELECT
    coupon_discount_types.pk_discount_type_id,
    coupon_discount_types.discount_type_code, 
    coupon_discount_types.discount_type_label, 
    coupon_discount_types.display_order, 
    coupon_discount_types.is_active, 
    coupon_discount_types.created_at_utc,
    CONVERT_TZ(coupon_discount_types.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    coupon_discount_types.updated_at_utc,
    CONVERT_TZ(coupon_discount_types.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM coupon_discount_types;

-- ==============================================================
-- 11. users
-- ==============================================================
SELECT
    users.pk_user_id,
    users.first_name,
    users.last_name,
    users.email,
    users.mobile,
    users.password_hash,
    users.google_id,
    users.email_verified,
    users.last_login_at_utc,
    CONVERT_TZ(users.last_login_at_utc, '+00:00', '+05:30') AS last_login_at_ist,
    users.is_active,
    users.created_at_utc,
    CONVERT_TZ(users.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    users.updated_at_utc,
    CONVERT_TZ(users.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM users;

-- ==============================================================
-- 12. user_roles
-- ==============================================================
SELECT
    user_roles.pk_user_role_id,
    user_roles.fk_user_id,
    user_roles.fk_role_id,
    user_roles.is_active,
    user_roles.created_at_utc,
    CONVERT_TZ(user_roles.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    user_roles.updated_at_utc,
    CONVERT_TZ(user_roles.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM user_roles;

-- ==============================================================
-- 13. vendors
-- ==============================================================
SELECT
    vendors.pk_vendor_id,
    vendors.fk_user_id,
    vendors.business_name,
    vendors.store_name,
    vendors.gst_number,
    vendors.business_description,
    vendors.is_active,
    vendors.created_at_utc,
    CONVERT_TZ(vendors.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    vendors.updated_at_utc,
    CONVERT_TZ(vendors.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM vendors;

-- ==============================================================
-- 14. vendor_bank_details
-- ==============================================================
SELECT
    vendor_bank_details.pk_vendor_bank_detail_id,
    vendor_bank_details.fk_vendor_id,
    vendor_bank_details.account_holder_name,
    vendor_bank_details.bank_name,
    vendor_bank_details.account_number,
    vendor_bank_details.ifsc_code,
    vendor_bank_details.branch_name,
    vendor_bank_details.is_active,
    vendor_bank_details.created_at_utc,
    CONVERT_TZ(vendor_bank_details.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    vendor_bank_details.updated_at_utc,
    CONVERT_TZ(vendor_bank_details.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM vendor_bank_details;

-- ==============================================================
-- 15. addresses
-- ==============================================================
SELECT
    addresses.pk_address_id,
    addresses.fk_user_id,
    addresses.fk_address_type_id,
    addresses.address_line_1,
    addresses.address_line_2,
    addresses.landmark,
    addresses.city,
    addresses.state,
    addresses.postal_code,
    addresses.country,
    addresses.is_default,
    addresses.is_active,
    addresses.created_at_utc,
    CONVERT_TZ(addresses.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    addresses.updated_at_utc,
    CONVERT_TZ(addresses.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM addresses;

-- ==============================================================
-- 16. brands
-- ==============================================================
SELECT
    brands.pk_brand_id,
    brands.brand_name,
    brands.brand_description,
    brands.is_active,
    brands.created_at_utc,
    CONVERT_TZ(brands.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    brands.updated_at_utc,
    CONVERT_TZ(brands.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM brands;

-- ==============================================================
-- 17. categories
-- ==============================================================
SELECT
    categories.pk_category_id,
    categories.fk_parent_category_id,
    categories.category_name,
    categories.category_description,
    categories.display_order,
    categories.is_active,
    categories.created_at_utc,
    CONVERT_TZ(categories.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    categories.updated_at_utc,
    CONVERT_TZ(categories.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM categories;

-- ==============================================================
-- 18. products
-- ==============================================================
SELECT
    products.pk_product_id,
    products.fk_vendor_id,
    products.fk_category_id,
    products.fk_brand_id,
    products.title,
    products.description,
    products.sku,
    products.price,
    products.discount_price,
    products.stock_quantity,
    products.average_rating,
    products.total_reviews,
    products.is_active,
    products.created_at_utc,
    CONVERT_TZ(products.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    products.updated_at_utc,
    CONVERT_TZ(products.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM products;

-- ==============================================================
-- 19. product_images
-- ==============================================================
SELECT
    product_images.pk_product_image_id,
    product_images.fk_product_id,
    product_images.image_url,
    product_images.image_data,
    product_images.file_name,
    product_images.mime_type,
    product_images.file_size_bytes,
    product_images.width_pixels,
    product_images.height_pixels,
    product_images.alt_text,
    product_images.is_primary,
    product_images.display_order,
    product_images.is_active,
    product_images.created_at_utc,
    CONVERT_TZ(product_images.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    product_images.updated_at_utc,
    CONVERT_TZ(product_images.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM product_images;

-- ==============================================================
-- 20. inventory
-- ==============================================================
SELECT
    inventory.pk_inventory_id,
    inventory.fk_product_id,
    inventory.available_quantity,
    inventory.reserved_quantity,
    inventory.low_stock_threshold,
    inventory.is_active,
    inventory.created_at_utc,
    CONVERT_TZ(inventory.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    inventory.updated_at_utc,
    CONVERT_TZ(inventory.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM inventory;

-- ==============================================================
-- 21. inventory_transactions
-- ==============================================================
SELECT
    inventory_transactions.pk_inventory_transaction_id,
    inventory_transactions.fk_product_id,
    inventory_transactions.fk_transaction_type_id,
    inventory_transactions.quantity,
    inventory_transactions.reference_type,
    inventory_transactions.reference_id) AS reference_id,
    inventory_transactions.remarks,
    inventory_transactions.is_active,
    inventory_transactions.created_at_utc,
    CONVERT_TZ(inventory_transactions.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    inventory_transactions.updated_at_utc,
    CONVERT_TZ(inventory_transactions.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM inventory_transactions;

-- ==============================================================
-- 39. inventory_reservations
-- ==============================================================
SELECT
    inventory_reservations.pk_reservation_id,
    inventory_reservations.fk_product_id,
    inventory_reservations.fk_cart_id,
    inventory_reservations.fk_cart_item_id,
    inventory_reservations.reserved_quantity,
    inventory_reservations.expires_at_utc,
    CONVERT_TZ(inventory_reservations.expires_at_utc, '+00:00', '+05:30') AS expires_at_ist,
    inventory_reservations.is_active,
    inventory_reservations.created_at_utc,
    CONVERT_TZ(inventory_reservations.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    inventory_reservations.updated_at_utc,
    CONVERT_TZ(inventory_reservations.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM inventory_reservations;

-- ==============================================================
-- 22. carts
-- ==============================================================
SELECT
    carts.pk_cart_id,
    carts.fk_user_id,
    carts.is_active,
    carts.created_at_utc,
    CONVERT_TZ(carts.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    carts.updated_at_utc,
    CONVERT_TZ(carts.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM carts;

-- ==============================================================
-- 23. cart_items
-- ==============================================================
SELECT
    cart_items.pk_cart_item_id,
    cart_items.fk_cart_id,
    cart_items.fk_product_id,
    cart_items.quantity,
    cart_items.is_saved_for_later,
    cart_items.is_active,
    cart_items.created_at_utc,
    CONVERT_TZ(cart_items.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    cart_items.updated_at_utc,
    CONVERT_TZ(cart_items.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM cart_items;

-- ==============================================================
-- 24. wishlists
-- ==============================================================
SELECT
    wishlists.pk_wishlist_id,
    wishlists.fk_user_id,
    wishlists.is_active,
    wishlists.created_at_utc,
    CONVERT_TZ(wishlists.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    wishlists.updated_at_utc,
    CONVERT_TZ(wishlists.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM wishlists;

-- ==============================================================
-- 25. wishlist_items
-- ==============================================================
SELECT
    wishlist_items.pk_wishlist_item_id,
    wishlist_items.fk_wishlist_id,
    wishlist_items.fk_product_id,
    wishlist_items.is_active,
    wishlist_items.created_at_utc,
    CONVERT_TZ(wishlist_items.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    wishlist_items.updated_at_utc,
    CONVERT_TZ(wishlist_items.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM wishlist_items;

-- ==============================================================
-- 40. wishlist_price_history
-- ==============================================================
SELECT
    wishlist_price_history.pk_history_id,
    wishlist_price_history.fk_wishlist_item_id,
    wishlist_price_history.price_snapshot,
    wishlist_price_history.price_drop_percentage,
    wishlist_price_history.snapshot_at_utc,
    CONVERT_TZ(wishlist_price_history.snapshot_at_utc, '+00:00', '+05:30') AS snapshot_at_ist,
    wishlist_price_history.is_active,
    wishlist_price_history.created_at_utc,
    CONVERT_TZ(wishlist_price_history.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    wishlist_price_history.updated_at_utc,
    CONVERT_TZ(wishlist_price_history.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM wishlist_price_history;

-- ==============================================================
-- 26. orders
-- ==============================================================
SELECT
    orders.pk_order_id,
    orders.fk_user_id,
    orders.fk_status_id,
    orders.fk_address_id,
    orders.total_amount,
    orders.is_active,
    orders.created_at_utc,
    CONVERT_TZ(orders.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    orders.updated_at_utc,
    CONVERT_TZ(orders.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM orders;

-- ==============================================================
-- 27. order_items
-- ==============================================================
SELECT
    order_items.pk_order_item_id,
    order_items.fk_order_id,
    order_items.fk_product_id,
    order_items.product_name_snapshot,
    order_items.quantity,
    order_items.unit_price,
    order_items.is_active,
    order_items.created_at_utc,
    CONVERT_TZ(order_items.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    order_items.updated_at_utc,
    CONVERT_TZ(order_items.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM order_items;

-- ==============================================================
-- 28. payments
-- ==============================================================
SELECT
    payments.pk_payment_id,
    payments.fk_order_id,
    payments.fk_status_id,
    payments.fk_gateway_id,
    payments.payment_method,
    payments.gateway_transaction_id,
    payments.gateway_order_id,
    payments.gateway_payment_link_id,
    payments.gateway_signature,
    payments.amount_paid,
    payments.paid_at_utc,
    CONVERT_TZ(payments.paid_at_utc, '+00:00', '+05:30') AS paid_at_ist,
    payments.is_active,
    payments.created_at_utc,
    CONVERT_TZ(payments.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    payments.updated_at_utc,
    CONVERT_TZ(payments.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM payments;

-- ==============================================================
-- 41. coupons
-- ==============================================================
SELECT
    coupons.pk_coupon_id,
    coupons.coupon_code,
    coupons.description,
    coupons.fk_discount_type_id,
    coupons.fk_coupon_status_id,
    coupons.discount_value,
    coupons.maximum_discount_amount,
    coupons.minimum_purchase_amount,
    coupons.maximum_global_usage,
    coupons.maximum_user_usage,
    coupons.usage_count,
    coupons.is_one_time,
    coupons.starts_at_utc,
    CONVERT_TZ(coupons.starts_at_utc, '+00:00', '+05:30') AS starts_at_ist,
    coupons.expires_at_utc,
    CONVERT_TZ(coupons.expires_at_utc, '+00:00', '+05:30') AS expires_at_ist,
    coupons.is_active,
    coupons.created_at_utc,
    CONVERT_TZ(coupons.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    coupons.updated_at_utc,
    CONVERT_TZ(coupons.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM coupons;

-- ==============================================================
-- 42. coupon_usage_history
-- ==============================================================
SELECT
    coupon_usage_history.pk_coupon_usage_id,
    coupon_usage_history.fk_coupon_id,
    coupon_usage_history.fk_user_id,
    coupon_usage_history.fk_cart_id,
    coupon_usage_history.fk_order_id,
    coupon_usage_history.discount_amount,
    coupon_usage_history.used_at_utc,
    CONVERT_TZ(coupon_usage_history.used_at_utc, '+00:00', '+05:30') AS used_at_ist,
    coupon_usage_history.is_active,
    coupon_usage_history.created_at_utc,
    CONVERT_TZ(coupon_usage_history.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    coupon_usage_history.updated_at_utc,
    CONVERT_TZ(coupon_usage_history.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM coupon_usage_history;

-- ==============================================================
-- 44. shipping_methods
-- ==============================================================
SELECT
    shipping_methods.pk_shipping_method_id,
    shipping_methods.shipping_method_name,
    shipping_methods.description,
    shipping_methods.base_cost,
    shipping_methods.cost_per_kg,
    shipping_methods.estimated_delivery_days,
    shipping_methods.is_active,
    shipping_methods.created_at_utc,
    CONVERT_TZ(shipping_methods.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    shipping_methods.updated_at_utc,
    CONVERT_TZ(shipping_methods.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM shipping_methods;

-- ==============================================================
-- 44. tax_configurations
-- ==============================================================
SELECT
    tax_configurations.pk_tax_configuration_id,
    tax_configurations.region_name,
    tax_configurations.tax_percentage,
    tax_configurations.effective_from,
    tax_configurations.effective_to,
    tax_configurations.is_active,
    tax_configurations.created_at_utc,
    CONVERT_TZ(tax_configurations.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    tax_configurations.updated_at_utc,
    CONVERT_TZ(tax_configurations.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM tax_configurations;

-- ==============================================================
-- 29. reviews
-- ==============================================================
SELECT
    reviews.pk_review_id,
    reviews.fk_product_id,
    reviews.fk_user_id,
    reviews.fk_status_id,
    reviews.rating,
    reviews.review_title,
    reviews.review_text,
    reviews.is_active,
    reviews.created_at_utc,
    CONVERT_TZ(reviews.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    reviews.updated_at_utc,
    CONVERT_TZ(reviews.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM reviews;

-- ==============================================================
-- 30. email_verification_tokens
-- ==============================================================
SELECT
    email_verification_tokens.pk_verification_token_id,
    email_verification_tokens.fk_user_id,
    email_verification_tokens.verification_token,
    email_verification_tokens.expires_at_utc,
    CONVERT_TZ(email_verification_tokens.expires_at_utc, '+00:00', '+05:30') AS expires_at_ist,
    email_verification_tokens.is_used,
    email_verification_tokens.is_active,
    email_verification_tokens.created_at_utc,
    CONVERT_TZ(email_verification_tokens.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    email_verification_tokens.updated_at_utc,
    CONVERT_TZ(email_verification_tokens.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM email_verification_tokens;

-- ==============================================================
-- 31. refresh_tokens
-- ==============================================================
SELECT
    refresh_tokens.pk_refresh_token_id,
    refresh_tokens.fk_user_id,
    refresh_tokens.token_hash,
    refresh_tokens.expires_at_utc,
    CONVERT_TZ(refresh_tokens.expires_at_utc, '+00:00', '+05:30') AS expires_at_ist,
    refresh_tokens.revoked_at_utc,
    CONVERT_TZ(refresh_tokens.revoked_at_utc, '+00:00', '+05:30') AS revoked_at_ist,
    refresh_tokens.is_active,
    refresh_tokens.created_at_utc,
    CONVERT_TZ(refresh_tokens.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    refresh_tokens.updated_at_utc,
    CONVERT_TZ(refresh_tokens.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM refresh_tokens;

-- ==============================================================
-- 32. password_reset_tokens
-- ==============================================================
SELECT
    password_reset_tokens.pk_reset_token_id,
    password_reset_tokens.fk_user_id,
    password_reset_tokens.token_hash,
    password_reset_tokens.expires_at_utc,
    CONVERT_TZ(password_reset_tokens.expires_at_utc, '+00:00', '+05:30') AS expires_at_ist,
    password_reset_tokens.is_used,
    password_reset_tokens.is_active,
    password_reset_tokens.created_at_utc,
    CONVERT_TZ(password_reset_tokens.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    password_reset_tokens.updated_at_utc,
    CONVERT_TZ(password_reset_tokens.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM password_reset_tokens;

-- ==============================================================
-- 33. notifications
-- ==============================================================
SELECT
    notifications.pk_notification_id,
    notifications.fk_user_id,
    notifications.fk_type_id,
    notifications.fk_channel_i,
    notifications.title,
    notifications.message,
    notifications.is_read,
    notifications.sent_at_utc,
    CONVERT_TZ(notifications.sent_at_utc, '+00:00', '+05:30') AS sent_at_ist,
    notifications.is_active,
    notifications.created_at_utc,
    CONVERT_TZ(notifications.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    notifications.updated_at_utc,
    CONVERT_TZ(notifications.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM notifications;

-- ==============================================================
-- 34. reports
-- ==============================================================
SELECT
    reports.pk_report_id,
    reports.fk_user_id,
    reports.fk_report_type_id,
    reports.report_name,
    reports.file_url,
    reports.generated_at_utc,
    CONVERT_TZ(reports.generated_at_utc, '+00:00', '+05:30') AS generated_at_ist,
    reports.is_active,
    reports.created_at_utc,
    CONVERT_TZ(reports.created_at_utc, '+00:00', '+05:30') AS created_at_ist,
    reports.updated_at_utc,
    CONVERT_TZ(reports.updated_at_utc, '+00:00', '+05:30') AS updated_at_ist
FROM reports;



