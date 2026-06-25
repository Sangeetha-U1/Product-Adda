/*============================================================================
4. SELECT FROM JOINED TABLES (ALL COLUMNS COMPREHENSIVE - ALL 34 TABLES)
============================================================================*/

-- ==============================================================
-- 1. SECURITY & ACCESS: users + user_roles + roles
-- Description: Complete User IAM mapping with all raw fields.
-- ==============================================================
SELECT
    -- --- USERS ---
    BIN_TO_UUID(users.pk_user_id) AS pk_user_id,
    users.first_name, users.last_name, users.email, roles.role_name, users.mobile, users.password_hash, users.google_id, users.email_verified, users.last_login_at_utc,
    CONVERT_TZ(users.last_login_at_utc, '+00:00', '+05:30') AS users_last_login_at_ist, users.is_active AS users_is_active,
    users.created_at_utc AS users_created_at_utc, CONVERT_TZ(users.created_at_utc, '+00:00', '+05:30') AS users_created_at_ist,
    users.updated_at_utc AS users_updated_at_utc, CONVERT_TZ(users.updated_at_utc, '+00:00', '+05:30') AS users_updated_at_ist,
    -- --- USER_ROLES ---
    BIN_TO_UUID(user_roles.pk_user_role_id) AS pk_user_role_id, BIN_TO_UUID(user_roles.fk_user_id) AS user_roles_fk_user_id, BIN_TO_UUID(user_roles.fk_role_id) AS user_roles_fk_role_id,
    user_roles.is_active AS user_roles_is_active, user_roles.created_at_utc AS user_roles_created_at_utc, CONVERT_TZ(user_roles.created_at_utc, '+00:00', '+05:30') AS user_roles_created_at_ist,
    user_roles.updated_at_utc AS user_roles_updated_at_utc, CONVERT_TZ(user_roles.updated_at_utc, '+00:00', '+05:30') AS user_roles_updated_at_ist,
    -- --- ROLES ---
    BIN_TO_UUID(roles.pk_role_id) AS pk_role_id, roles.role_name, roles.description AS roles_description, roles.is_active AS roles_is_active,
    roles.created_at_utc AS roles_created_at_utc, CONVERT_TZ(roles.created_at_utc, '+00:00', '+05:30') AS roles_created_at_ist,
    roles.updated_at_utc AS roles_updated_at_utc, CONVERT_TZ(roles.updated_at_utc, '+00:00', '+05:30') AS roles_updated_at_ist
FROM users
INNER JOIN user_roles ON user_roles.fk_user_id = users.pk_user_id
INNER JOIN roles ON roles.pk_role_id = user_roles.fk_role_id;


-- ==============================================================
-- 2. VENDOR ECOSYSTEM: vendors + users + vendor_bank_details
-- Description: Complete mapping of business entities to their owner users and legal bank accounts.
-- ==============================================================
SELECT
    -- --- VENDORS ---
    BIN_TO_UUID(vendors.pk_vendor_id) AS pk_vendor_id, BIN_TO_UUID(vendors.fk_user_id) AS vendors_fk_user_id, vendors.business_name, vendors.store_name, vendors.gst_number, vendors.business_description, vendors.is_active AS vendors_is_active,
    vendors.created_at_utc AS vendors_created_at_utc, CONVERT_TZ(vendors.created_at_utc, '+00:00', '+05:30') AS vendors_created_at_ist, vendors.updated_at_utc AS vendors_updated_at_utc, CONVERT_TZ(vendors.updated_at_utc, '+00:00', '+05:30') AS vendors_updated_at_ist,
    -- --- USERS (OWNER) ---
    users.first_name AS owner_first_name, users.last_name AS owner_last_name, users.email AS owner_email, users.mobile AS owner_mobile,
    -- --- VENDOR_BANK_DETAILS ---
    BIN_TO_UUID(vendor_bank_details.pk_vendor_bank_detail_id) AS pk_vendor_bank_detail_id, vendor_bank_details.account_holder_name, vendor_bank_details.bank_name, vendor_bank_details.account_number, vendor_bank_details.ifsc_code, vendor_bank_details.branch_name, vendor_bank_details.is_active AS bank_is_active,
    vendor_bank_details.created_at_utc AS bank_created_at_utc, CONVERT_TZ(vendor_bank_details.created_at_utc, '+00:00', '+05:30') AS bank_created_at_ist
FROM vendors
INNER JOIN users ON vendors.fk_user_id = users.pk_user_id
LEFT JOIN vendor_bank_details ON vendor_bank_details.fk_vendor_id = vendors.pk_vendor_id;


-- ==============================================================
-- 3. USER GEOGRAPHY: addresses + address_types + users
-- Description: Complete addresses linked with address classification data.
-- ==============================================================
SELECT
    -- --- ADDRESSES ---
    BIN_TO_UUID(addresses.pk_address_id) AS pk_address_id, BIN_TO_UUID(addresses.fk_user_id) AS addresses_fk_user_id, BIN_TO_UUID(addresses.fk_address_type_id) AS addresses_fk_address_type_id, addresses.address_line_1, addresses.address_line_2, addresses.landmark, addresses.city, addresses.state, addresses.postal_code, addresses.country, addresses.is_default, addresses.is_active AS addresses_is_active,
    addresses.created_at_utc AS addresses_created_at_utc, CONVERT_TZ(addresses.created_at_utc, '+00:00', '+05:30') AS addresses_created_at_ist,
    -- --- ADDRESS_TYPES ---
    BIN_TO_UUID(address_types.pk_address_type_id) AS pk_address_type_id, address_types.address_type_name, address_types.description AS address_type_desc, address_types.is_active AS address_type_is_active,
    -- --- USERS ---
    users.first_name, users.last_name, users.email
FROM addresses
INNER JOIN address_types ON addresses.fk_address_type_id = address_types.pk_address_type_id
INNER JOIN users ON addresses.fk_user_id = users.pk_user_id;


-- ==============================================================
-- 4. PRODUCT CORE: products + categories + brands + product_images
-- Description: Core inventory catalog listing all items, structural nodes, and asset records.
-- ==============================================================
SELECT
    -- --- PRODUCTS ---
    BIN_TO_UUID(products.pk_product_id) AS pk_product_id, BIN_TO_UUID(products.fk_vendor_id) AS products_fk_vendor_id, BIN_TO_UUID(products.fk_category_id) AS products_fk_category_id, BIN_TO_UUID(products.fk_brand_id) AS products_fk_brand_id, products.title, products.description AS products_description, products.sku, products.price, products.discount_price, products.stock_quantity, products.average_rating, products.total_reviews, products.is_active AS products_is_active,
    products.created_at_utc AS products_created_at_utc, CONVERT_TZ(products.created_at_utc, '+00:00', '+05:30') AS products_created_at_ist,
    -- --- CATEGORIES ---
    BIN_TO_UUID(categories.pk_category_id) AS pk_category_id, BIN_TO_UUID(categories.fk_parent_category_id) AS fk_parent_category_id, categories.category_name, categories.category_description, categories.display_order AS cat_display_order, categories.is_active AS categories_is_active,
    -- --- BRANDS ---
    BIN_TO_UUID(brands.pk_brand_id) AS pk_brand_id, brands.brand_name, brands.brand_description, brands.is_active AS brands_is_active,
    -- --- PRODUCT_IMAGES ---
    BIN_TO_UUID(product_images.pk_product_image_id) AS pk_product_image_id, product_images.image_url, product_images.file_name, product_images.mime_type, product_images.file_size_bytes, product_images.alt_text, product_images.is_primary, product_images.display_order AS img_display_order, product_images.is_active AS img_is_active
FROM products
LEFT JOIN categories ON products.fk_category_id = categories.pk_category_id
LEFT JOIN brands ON products.fk_brand_id = brands.pk_brand_id
LEFT JOIN product_images ON product_images.fk_product_id = products.pk_product_id;


-- ==============================================================
-- 5. STOCK METRICS: inventory + inventory_transactions + inventory_transaction_types + products
-- Description: Full audit log tracking actual static item stocks vs transaction modifications.
-- ==============================================================
SELECT
    -- --- INVENTORY ---
    BIN_TO_UUID(inventory.pk_inventory_id) AS pk_inventory_id, BIN_TO_UUID(inventory.fk_product_id) AS inventory_fk_product_id, inventory.available_quantity, inventory.reserved_quantity, inventory.low_stock_threshold, inventory.is_active AS inventory_is_active,
    inventory.created_at_utc AS inventory_created_at_utc, CONVERT_TZ(inventory.created_at_utc, '+00:00', '+05:30') AS inventory_created_at_ist,
    -- --- INVENTORY_TRANSACTIONS ---
    BIN_TO_UUID(inventory_transactions.pk_inventory_transaction_id) AS pk_inventory_transaction_id, BIN_TO_UUID(inventory_transactions.fk_transaction_type_id) AS trans_fk_type_id, inventory_transactions.quantity AS trans_quantity, inventory_transactions.reference_type, BIN_TO_UUID(inventory_transactions.reference_id) AS reference_id, inventory_transactions.remarks, inventory_transactions.is_active AS trans_is_active,
    inventory_transactions.created_at_utc AS trans_created_at_utc, CONVERT_TZ(inventory_transactions.created_at_utc, '+00:00', '+05:30') AS trans_created_at_ist,
    -- --- INVENTORY_TRANSACTION_TYPES ---
    BIN_TO_UUID(inventory_transaction_types.pk_transaction_type_id) AS pk_transaction_type_id, inventory_transaction_types.transaction_type_name, inventory_transaction_types.description AS trans_type_desc,
    -- --- PRODUCTS SNIPPET ---
    products.title AS product_title, products.sku AS product_sku
FROM inventory
INNER JOIN products ON inventory.fk_product_id = products.pk_product_id
LEFT JOIN inventory_transactions ON inventory_transactions.fk_product_id = products.pk_product_id
LEFT JOIN inventory_transaction_types ON inventory_transactions.fk_transaction_type_id = inventory_transaction_types.pk_transaction_type_id;


-- ==============================================================
-- 6. SHOPPING CARTS: carts + cart_items + users + products
-- Description: Live unpurchased cart payloads matching users with chosen inventory segments.
-- ==============================================================
SELECT
    -- --- CARTS ---
    BIN_TO_UUID(carts.pk_cart_id) AS pk_cart_id, BIN_TO_UUID(carts.fk_user_id) AS carts_fk_user_id, carts.is_active AS carts_is_active,
    carts.created_at_utc AS carts_created_at_utc, CONVERT_TZ(carts.created_at_utc, '+00:00', '+05:30') AS carts_created_at_ist,
    -- --- CART_ITEMS ---
    BIN_TO_UUID(cart_items.pk_cart_item_id) AS pk_cart_item_id, BIN_TO_UUID(cart_items.fk_cart_id) AS cart_items_fk_cart_id, BIN_TO_UUID(cart_items.fk_product_id) AS cart_items_fk_product_id, cart_items.quantity, cart_items.is_saved_for_later, cart_items.is_active AS cart_items_is_active,
    cart_items.created_at_utc AS cart_items_created_at_utc, CONVERT_TZ(cart_items.created_at_utc, '+00:00', '+05:30') AS cart_items_created_at_ist,
    -- --- USERS ---
    users.first_name, users.last_name, users.email,
    -- --- PRODUCTS ---
    products.title, products.sku, products.price
FROM carts
INNER JOIN cart_items ON cart_items.fk_cart_id = carts.pk_cart_id
INNER JOIN users ON carts.fk_user_id = users.pk_user_id
INNER JOIN products ON cart_items.fk_product_id = products.pk_product_id;


-- ==============================================================
-- 7. ENGAGEMENT INTEREST: wishlists + wishlist_items + users + products
-- Description: Intent monitoring lists parsing what users actively watch.
-- ==============================================================
SELECT
    -- --- WISHLISTS ---
    BIN_TO_UUID(wishlists.pk_wishlist_id) AS pk_wishlist_id, BIN_TO_UUID(wishlists.fk_user_id) AS wishlists_fk_user_id, wishlists.is_active AS wishlists_is_active,
    wishlists.created_at_utc AS wishlists_created_at_utc, CONVERT_TZ(wishlists.created_at_utc, '+00:00', '+05:30') AS wishlists_created_at_ist,
    -- --- WISHLIST_ITEMS ---
    BIN_TO_UUID(wishlist_items.pk_wishlist_item_id) AS pk_wishlist_item_id, BIN_TO_UUID(wishlist_items.fk_wishlist_id) AS wishlist_items_fk_wishlist_id, BIN_TO_UUID(wishlist_items.fk_product_id) AS wishlist_items_fk_product_id, wishlist_items.is_active AS wishlist_items_is_active,
    wishlist_items.created_at_utc AS wishlist_items_created_at_utc, CONVERT_TZ(wishlist_items.created_at_utc, '+00:00', '+05:30') AS wishlist_items_created_at_ist,
    -- --- USERS ---
    users.first_name, users.last_name, users.email,
    -- --- PRODUCTS ---
    products.title, products.sku, products.price
FROM wishlists
INNER JOIN wishlist_items ON wishlist_items.fk_wishlist_id = wishlists.pk_wishlist_id
INNER JOIN users ON wishlists.fk_user_id = users.pk_user_id
INNER JOIN products ON wishlist_items.fk_product_id = products.pk_product_id;


-- ==============================================================
-- 8. SALES ORDER ENGINE: orders + order_items + order_statuses + addresses + users
-- Description: Master Transaction pipeline matching historical invoices against buyers and items.
-- ==============================================================
SELECT
    -- --- ORDERS ---
    BIN_TO_UUID(orders.pk_order_id) AS pk_order_id, BIN_TO_UUID(orders.fk_user_id) AS orders_fk_user_id, BIN_TO_UUID(orders.fk_status_id) AS orders_fk_status_id, BIN_TO_UUID(orders.fk_address_id) AS orders_fk_address_id, orders.total_amount, orders.is_active AS orders_is_active,
    orders.created_at_utc AS orders_created_at_utc, CONVERT_TZ(orders.created_at_utc, '+00:00', '+05:30') AS orders_created_at_ist,
    -- --- ORDER_ITEMS ---
    BIN_TO_UUID(order_items.pk_order_item_id) AS pk_order_item_id, BIN_TO_UUID(order_items.fk_product_id) AS order_items_fk_product_id, order_items.product_name_snapshot, order_items.quantity, order_items.unit_price, order_items.is_active AS order_items_is_active,
    -- --- ORDER_STATUSES ---
    BIN_TO_UUID(order_statuses.pk_status_id) AS pk_status_id, order_statuses.status_name, order_statuses.description AS status_desc,
    -- --- ADDRESSES ---
    addresses.address_line_1, addresses.city, addresses.state, addresses.postal_code,
    -- --- USERS ---
    users.first_name, users.last_name, users.email
FROM orders
INNER JOIN order_items ON order_items.fk_order_id = orders.pk_order_id
INNER JOIN order_statuses ON orders.fk_status_id = order_statuses.pk_status_id
LEFT JOIN addresses ON orders.fk_address_id = addresses.pk_address_id
INNER JOIN users ON orders.fk_user_id = users.pk_user_id;


-- ==============================================================
-- 9. REVENUE LOGISTICS: payments + payment_statuses + payment_gateways + orders
-- Description: Financial compliance processing tracing incoming receipts to invoices.
-- ==============================================================
SELECT
    -- --- PAYMENTS ---
    BIN_TO_UUID(payments.pk_payment_id) AS pk_payment_id, BIN_TO_UUID(payments.fk_order_id) AS payments_fk_order_id, BIN_TO_UUID(payments.fk_status_id) AS payments_fk_status_id, BIN_TO_UUID(payments.fk_gateway_id) AS payments_fk_gateway_id, payments.payment_method, payments.gateway_transaction_id, payments.gateway_order_id, payments.gateway_payment_link_id, payments.gateway_signature, payments.amount_paid, payments.paid_at_utc, CONVERT_TZ(payments.paid_at_utc, '+00:00', '+05:30') AS paid_at_ist, payments.is_active AS payments_is_active,
    payments.created_at_utc AS payments_created_at_utc, CONVERT_TZ(payments.created_at_utc, '+00:00', '+05:30') AS payments_created_at_ist,
    -- --- PAYMENT_STATUSES ---
    BIN_TO_UUID(payment_statuses.pk_status_id) AS pk_status_id, payment_statuses.status_name AS payment_status_name, payment_statuses.description AS payment_status_desc,
    -- --- PAYMENT_GATEWAYS ---
    BIN_TO_UUID(payment_gateways.pk_gateway_id) AS pk_gateway_id, payment_gateways.gateway_name, payment_gateways.description AS gateway_desc,
    -- --- ORDERS SNAPSHOT ---
    orders.total_amount AS order_grand_total
FROM payments
INNER JOIN payment_statuses ON payments.fk_status_id = payment_statuses.pk_status_id
INNER JOIN payment_gateways ON payments.fk_gateway_id = payment_gateways.pk_gateway_id
INNER JOIN orders ON payments.fk_order_id = orders.pk_order_id;


-- ==============================================================
-- 10. SOCIAL PROOF: reviews + review_statuses + products + users
-- Description: Complete UGC (User Generated Content) tracking containing raw evaluation data.
-- ==============================================================
SELECT
    -- --- REVIEWS ---
    BIN_TO_UUID(reviews.pk_review_id) AS pk_review_id, BIN_TO_UUID(reviews.fk_product_id) AS reviews_fk_product_id, BIN_TO_UUID(reviews.fk_user_id) AS reviews_fk_user_id, BIN_TO_UUID(reviews.fk_status_id) AS reviews_fk_status_id, reviews.rating, reviews.review_title, reviews.review_text, reviews.is_active AS reviews_is_active,
    reviews.created_at_utc AS reviews_created_at_utc, CONVERT_TZ(reviews.created_at_utc, '+00:00', '+05:30') AS reviews_created_at_ist,
    -- --- REVIEW_STATUSES ---
    BIN_TO_UUID(review_statuses.pk_status_id) AS pk_status_id, review_statuses.status_name AS review_status_name, review_statuses.description AS review_status_desc,
    -- --- PRODUCTS ---
    products.title AS product_title,
    -- --- USERS ---
    users.first_name, users.last_name, users.email
FROM reviews
INNER JOIN review_statuses ON reviews.fk_status_id = review_statuses.pk_status_id
INNER JOIN products ON reviews.fk_product_id = products.pk_product_id
INNER JOIN users ON reviews.fk_user_id = users.pk_user_id;


-- ==============================================================
-- 11. AUTH LIFE CYCLES: email_verification_tokens + refresh_tokens + password_reset_tokens + users
-- Description: Session security telemetry containing short-lived tokens.
-- ==============================================================
SELECT
    -- --- USERS CORE ---
    BIN_TO_UUID(users.pk_user_id) AS pk_user_id, users.first_name, users.last_name, users.email,
    -- --- EMAIL_VERIFICATION_TOKENS ---
    BIN_TO_UUID(email_verification_tokens.pk_verification_token_id) AS pk_verification_token_id, email_verification_tokens.verification_token, email_verification_tokens.expires_at_utc AS email_expires_utc, email_verification_tokens.is_used AS email_token_used,
    -- --- REFRESH_TOKENS ---
    BIN_TO_UUID(refresh_tokens.pk_refresh_token_id) AS pk_refresh_token_id, refresh_tokens.token_hash AS refresh_token_hash, refresh_tokens.expires_at_utc AS refresh_expires_utc, refresh_tokens.revoked_at_utc AS refresh_revoked_utc,
    -- --- PASSWORD_RESET_TOKENS ---
    BIN_TO_UUID(password_reset_tokens.pk_reset_token_id) AS pk_reset_token_id, password_reset_tokens.token_hash AS reset_token_hash, password_reset_tokens.expires_at_utc AS reset_expires_utc, password_reset_tokens.is_used AS reset_token_used
FROM users
LEFT JOIN email_verification_tokens ON email_verification_tokens.fk_user_id = users.pk_user_id
LEFT JOIN refresh_tokens ON refresh_tokens.fk_user_id = users.pk_user_id
LEFT JOIN password_reset_tokens ON password_reset_tokens.fk_user_id = users.pk_user_id;


-- ==============================================================
-- 12. COMMUNICATIONS LOG: notifications + notification_types + notification_channels + users
-- Description: System outreach metadata showing messages dispatched to targeted clients.
-- ==============================================================
SELECT
    -- --- NOTIFICATIONS ---
    BIN_TO_UUID(notifications.pk_notification_id) AS pk_notification_id, BIN_TO_UUID(notifications.fk_user_id) AS notifications_fk_user_id, BIN_TO_UUID(notifications.fk_type_id) AS notifications_fk_type_id, BIN_TO_UUID(notifications.fk_channel_id) AS notifications_fk_channel_id, notifications.title, notifications.message, notifications.is_read, notifications.sent_at_utc, CONVERT_TZ(notifications.sent_at_utc, '+00:00', '+05:30') AS sent_at_ist, notifications.is_active AS notifications_is_active,
    -- --- NOTIFICATION_TYPES ---
    BIN_TO_UUID(notification_types.pk_notification_type_id) AS pk_notification_type_id, notification_types.notification_type_name,
    -- --- NOTIFICATION_CHANNELS ---
    BIN_TO_UUID(notification_channels.pk_channel_id) AS pk_channel_id, notification_channels.channel_name,
    -- --- USERS ---
    users.first_name, users.last_name, users.email
FROM notifications
INNER JOIN notification_types ON notifications.fk_type_id = notification_types.pk_notification_type_id
INNER JOIN notification_channels ON notifications.fk_channel_id = notification_channels.pk_channel_id
INNER JOIN users ON notifications.fk_user_id = users.pk_user_id;


-- ==============================================================
-- 13. BI COMPLIANCE: reports + report_types + users
-- Description: Administrative export outputs mapped to internal operational classifications.
-- ==============================================================
SELECT
    -- --- REPORTS ---
    BIN_TO_UUID(reports.pk_report_id) AS pk_report_id, BIN_TO_UUID(reports.fk_user_id) AS reports_fk_user_id, BIN_TO_UUID(reports.fk_report_type_id) AS reports_fk_report_type_id, reports.report_name, reports.file_url, reports.generated_at_utc, CONVERT_TZ(reports.generated_at_utc, '+00:00', '+05:30') AS generated_at_ist, reports.is_active AS reports_is_active,
    -- --- REPORT_TYPES ---
    BIN_TO_UUID(report_types.pk_report_type_id) AS pk_report_type_id, report_types.report_type_name, report_types.description AS report_type_desc,
    -- --- USERS (GENERATED BY) ---
    users.first_name AS staff_first_name, users.last_name AS staff_last_name, users.email AS staff_email
FROM reports
INNER JOIN report_types ON reports.fk_report_type_id = report_types.pk_report_type_id
INNER JOIN users ON reports.fk_user_id = users.pk_user_id;

-- ==============================================================
-- 14. VENDOR-PRODUCT ORDER MATCHING: vendors + products + order_items + orders + users + roles
-- Description: Comprehensive tracking linking vendors to ordered products and user roles.
-- ==============================================================
SELECT
    -- --- VENDORS ---
    BIN_TO_UUID(vendors.pk_vendor_id) AS pk_vendor_id,
    vendors.business_name,
    vendors.store_name,
    
    -- --- PRODUCTS ---
    BIN_TO_UUID(products.pk_product_id) AS pk_product_id,
    products.title AS product_title,
    products.sku AS product_sku,
    products.price AS current_product_price,
    
    -- --- ORDER ITEMS (The Bridge) ---
    BIN_TO_UUID(order_items.pk_order_item_id) AS pk_order_item_id,
    order_items.product_name_snapshot,
    order_items.quantity AS ordered_quantity,
    order_items.unit_price AS purchased_unit_price,
    
    -- --- ORDERS ---
    BIN_TO_UUID(orders.pk_order_id) AS pk_order_id,
    orders.total_amount AS order_total_amount,
    order_statuses.status_name AS order_status,
    orders.created_at_utc AS order_created_at_utc,
    CONVERT_TZ(orders.created_at_utc, '+00:00', '+05:30') AS order_created_at_ist,
    
    -- --- CUSTOMER (The Buyer) ---
    BIN_TO_UUID(buyer.pk_user_id) AS buyer_user_id,
    buyer.first_name AS buyer_first_name,
    buyer.last_name AS buyer_last_name,
    buyer.email AS buyer_email,
    buyer_roles.role_name AS buyer_system_role,
    
    -- --- VENDOR OWNER (The Seller) ---
    BIN_TO_UUID(vendor_owner.pk_user_id) AS vendor_owner_user_id,
    vendor_owner.first_name AS seller_first_name,
    vendor_owner.last_name AS seller_last_name,
    vendor_owner.email AS seller_email,
    vendor_roles.role_name AS seller_system_role

FROM order_items
-- 1. Link order items back to their parent products and the vendors who own them
INNER JOIN products ON order_items.fk_product_id = products.pk_product_id
INNER JOIN vendors ON products.fk_vendor_id = vendors.pk_vendor_id

-- 2. Link order items to the main order record and its status
INNER JOIN orders ON order_items.fk_order_id = orders.pk_order_id
INNER JOIN order_statuses ON orders.fk_status_id = order_statuses.pk_status_id

-- 3. Link the buyer (the user who placed the order) and their role profile
INNER JOIN users AS buyer ON orders.fk_user_id = buyer.pk_user_id
INNER JOIN user_roles AS buyer_ur ON buyer.pk_user_id = buyer_ur.fk_user_id
INNER JOIN roles AS buyer_roles ON buyer_ur.fk_role_id = buyer_roles.pk_role_id

-- 4. Link the seller/owner of the business entity (vendor account) and their role profile
INNER JOIN users AS vendor_owner ON vendors.fk_user_id = vendor_owner.pk_user_id
INNER JOIN user_roles AS vendor_ur ON vendor_owner.pk_user_id = vendor_ur.fk_user_id
INNER JOIN roles AS vendor_roles ON vendor_ur.fk_role_id = vendor_roles.pk_role_id;