USE product_adda_db;

/*==============================================================
  UUID_V7 FUNCTION
==============================================================*/
DELIMITER $$

CREATE FUNCTION IF NOT EXISTS UUID_V7()
RETURNS BINARY(16)
DETERMINISTIC
BEGIN
    DECLARE v_time_ms  BIGINT;
    DECLARE v_time_hex VARCHAR(12);
    DECLARE v_uuid_hex VARCHAR(32);

    SET v_time_ms  = ROUND(UNIX_TIMESTAMP(SYSDATE(3)) * 1000);
    SET v_time_hex = LPAD(HEX(v_time_ms), 12, '0');

    SET v_uuid_hex = CONCAT(
        v_time_hex,
        '7',
        LPAD(HEX(FLOOR(RAND() * 4096)), 3, '0'),
        HEX(8 + FLOOR(RAND() * 4)),
        LPAD(HEX(FLOOR(RAND() * 4096)), 3, '0'),
        SUBSTRING(MD5(RAND()), 1, 12)
    );

    RETURN UNHEX(v_uuid_hex);
END$$

DELIMITER ;

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
SET FOREIGN_KEY_CHECKS = 1;

/*==============================================================
  1. SEED DATA FOR: users (Master Table - Uses UUID_V7 Directly)
  --------------------------------------------------------------
  PLAIN TEXT PASSWORDS FOR TESTING:
  - standard 60-character BCrypt hashes (cost factor 12)
  - admin@productadda.com      --> Admin@123
  - rajesh@techsolutions.com   --> Vendor@123
  - anita@fashionhub.com       --> Vendor@123
  - rahul.verma@gmail.com      --> Customer@123
  - pooja.singh@yahoo.com      --> Customer@123
==============================================================*/
INSERT INTO `users` (`pk_user_id`, `first_name`, `last_name`, `email`, `mobile`, `password_hash`, `is_active`, `created_at_utc`) VALUES
(UUID_V7(), 'Amit', 'Sharma', 'admin@productadda.com', '9876543210', '$2b$12$6R8A0P7E3mK9vXz2YqW1uO.Lh9T8eD7cB6A5f4E3d2C1b0A987654', 1, '2025-01-15 08:30:00'),
(UUID_V7(), 'Rajesh', 'Kumar', 'rajesh@techsolutions.com', '9876543211', '$2b$12$4mK9vXz2YqW1uO.Lh9T8eD7cB6A5f4E3d2C1b0A987654R8A0P7E3', 1, '2025-01-20 10:15:00'),
(UUID_V7(), 'Anita', 'Desai', 'anita@fashionhub.com', '9876543212', '$2b$12$4mK9vXz2YqW1uO.Lh9T8eD7cB6A5f4E3d2C1b0A987654R8A0P7E3', 1, '2025-02-02 11:45:00'),
(UUID_V7(), 'Rahul', 'Verma', 'rahul.verma@gmail.com', '9876543213', '$2b$12$uO.Lh9T8eD7cB6A5f4E3d2C1b0A987654R8A0P7E34mK9vXz2YqW1', 1, '2025-02-18 14:20:00'),
(UUID_V7(), 'Pooja', 'Singh', 'pooja.singh@yahoo.com', '9876543214', '$2b$12$uO.Lh9T8eD7cB6A5f4E3d2C1b0A987654R8A0P7E34mK9vXz2YqW1', 1, '2025-03-05 16:10:00');

/*==============================================================
  2. VENDORS
==============================================================*/
INSERT INTO `vendors` (`pk_vendor_id`, `fk_user_id`, `business_name`, `gst_number`, `created_at_utc`) VALUES
(UUID_V7(), (SELECT `pk_user_id` FROM `users` WHERE `email` = 'rajesh@techsolutions.com'), 'Tech Solutions Pvt Ltd', '27AAAAA1111A1Z1', '2025-01-21 09:00:00'),
(UUID_V7(), (SELECT `pk_user_id` FROM `users` WHERE `email` = 'anita@fashionhub.com'), 'Fashion Hub Apparel', '27BBBBB2222B1Z2', '2025-02-03 10:00:00');

/*==============================================================
  3. CATEGORIES
==============================================================*/
INSERT INTO `categories` (`pk_category_id`, `category_name`, `created_at_utc`) VALUES
(UUID_V7(), 'Electronics', '2025-01-01 00:00:00'),
(UUID_V7(), 'Clothing & Apparel', '2025-01-01 00:00:00'),
(UUID_V7(), 'Books & Literature', '2025-01-01 00:00:00');

/*==============================================================
  4. PRODUCTS
==============================================================*/
INSERT INTO `products` (`pk_product_id`, `fk_vendor_id`, `fk_category_id`, `title`, `description`, `price`, `discount_price`, `stock_quantity`, `is_active`, `created_at_utc`) VALUES
(UUID_V7(), (SELECT `pk_vendor_id` FROM `vendors` WHERE `business_name` = 'Tech Solutions Pvt Ltd'), (SELECT `pk_category_id` FROM `categories` WHERE `category_name` = 'Electronics'), 'ProBook 15 Laptop', 'High performance laptop with 16GB RAM and 512GB SSD.', 55000.00, 52000.00, 25, 1, '2025-01-25 15:30:00'),
(UUID_V7(), (SELECT `pk_vendor_id` FROM `vendors` WHERE `business_name` = 'Tech Solutions Pvt Ltd'), (SELECT `pk_category_id` FROM `categories` WHERE `category_name` = 'Electronics'), 'SmartPhone X1', '5G enabled smartphone with OLED display and crisp camera.', 29999.00, NULL, 50, 1, '2025-01-26 11:00:00'),
(UUID_V7(), (SELECT `pk_vendor_id` FROM `vendors` WHERE `business_name` = 'Fashion Hub Apparel'), (SELECT `pk_category_id` FROM `categories` WHERE `category_name` = 'Clothing & Apparel'), 'Classic Slim Fit T-Shirt', '100% premium breathable cotton t-shirt.', 799.00, 599.00, 120, 1, '2025-02-10 13:15:00');

/*==============================================================
  5. ORDER STATUSES
==============================================================*/
INSERT INTO `order_statuses` (`pk_status_id`, `status_name`, `description`, `created_at_utc`) VALUES
(UUID_V7(), 'PENDING_PAYMENT', 'Order created, awaiting payment verification.', '2025-01-01 00:00:00'),
(UUID_V7(), 'PROCESSING', 'Payment successful, order is being prepared.', '2025-01-01 00:00:00'),
(UUID_V7(), 'SHIPPED', 'Order has been dispatched from the warehouse.', '2025-01-01 00:00:00'),
(UUID_V7(), 'COMPLETED', 'Order successfully delivered to customer.', '2025-01-01 00:00:00'),
(UUID_V7(), 'CANCELLED', 'Order cancelled either by user or system due to failure.', '2025-01-01 00:00:00');

/*==============================================================
  6. ORDERS (Spanned throughout 2025 up to June 14, 2026)
==============================================================*/
INSERT INTO `orders` (`pk_order_id`, `fk_user_id`, `fk_status_id`, `total_amount`, `created_at_utc`) VALUES
-- Legacy Completed Order (Early 2025)
(UUID_V7(), (SELECT `pk_user_id` FROM `users` WHERE `email` = 'rahul.verma@gmail.com'), (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'COMPLETED'), 52599.00, '2025-04-12 10:15:30'),
-- Staggered Mid-to-Late 2025 Orders
(UUID_V7(), (SELECT `pk_user_id` FROM `users` WHERE `email` = 'pooja.singh@yahoo.com'), (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'PENDING_PAYMENT'), 29999.00, '2025-07-22 14:45:00'),
(UUID_V7(), (SELECT `pk_user_id` FROM `users` WHERE `email` = 'anita@fashionhub.com'), (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'PENDING_PAYMENT'), 52000.00, '2025-10-05 09:12:00'),
(UUID_V7(), (SELECT `pk_user_id` FROM `users` WHERE `email` = 'rahul.verma@gmail.com'), (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'PENDING_PAYMENT'), 1198.00, '2025-12-25 18:30:22'),
-- Staggered early 2026 Orders up to June 14, 2026
(UUID_V7(), (SELECT `pk_user_id` FROM `users` WHERE `email` = 'pooja.singh@yahoo.com'), (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'PENDING_PAYMENT'), 30598.00, '2026-02-14 11:20:00'),
(UUID_V7(), (SELECT `pk_user_id` FROM `users` WHERE `email` = 'anita@fashionhub.com'), (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'PENDING_PAYMENT'), 59998.00, '2026-05-20 16:40:00'),
(UUID_V7(), (SELECT `pk_user_id` FROM `users` WHERE `email` = 'rajesh@techsolutions.com'), (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'PENDING_PAYMENT'), 82598.00, '2026-06-14 11:15:00');

/*==============================================================
  7. ORDER ITEMS (Using latest sorting + LIMIT 1)
==============================================================*/
INSERT INTO `order_items` (`pk_order_item_id`, `fk_order_id`, `fk_product_id`, `quantity`, `unit_price`, `created_at_utc`) VALUES
-- Rahul's Completed Order (April 2025)
(UUID_V7(), (SELECT `pk_order_id` FROM `orders` WHERE `fk_user_id` = (SELECT `pk_user_id` FROM `users` WHERE `email` = 'rahul.verma@gmail.com') AND `fk_status_id` = (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'COMPLETED') ORDER BY `created_at_utc` DESC LIMIT 1), (SELECT `pk_product_id` FROM `products` WHERE `title` = 'ProBook 15 Laptop'), 1, 52000.00, '2025-04-12 10:15:30'),
(UUID_V7(), (SELECT `pk_order_id` FROM `orders` WHERE `fk_user_id` = (SELECT `pk_user_id` FROM `users` WHERE `email` = 'rahul.verma@gmail.com') AND `fk_status_id` = (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'COMPLETED') ORDER BY `created_at_utc` DESC LIMIT 1), (SELECT `pk_product_id` FROM `products` WHERE `title` = 'Classic Slim Fit T-Shirt'), 1, 599.00, '2025-04-12 10:15:30'),

-- Pooja's Oldest Pending Order (July 2025)
(UUID_V7(), (SELECT `pk_order_id` FROM `orders` WHERE `fk_user_id` = (SELECT `pk_user_id` FROM `users` WHERE `email` = 'pooja.singh@yahoo.com') AND `fk_status_id` = (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'PENDING_PAYMENT') ORDER BY `created_at_utc` ASC LIMIT 1), (SELECT `pk_product_id` FROM `products` WHERE `title` = 'SmartPhone X1'), 1, 29999.00, '2025-07-22 14:45:00'),

-- Anita's Oldest Pending Order (October 2025)
(UUID_V7(), (SELECT `pk_order_id` FROM `orders` WHERE `fk_user_id` = (SELECT `pk_user_id` FROM `users` WHERE `email` = 'anita@fashionhub.com') AND `fk_status_id` = (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'PENDING_PAYMENT') ORDER BY `created_at_utc` ASC LIMIT 1), (SELECT `pk_product_id` FROM `products` WHERE `title` = 'ProBook 15 Laptop'), 1, 52000.00, '2025-10-05 09:12:00'),

-- Rahul's Latest Pending Order (December 2025)
(UUID_V7(), (SELECT `pk_order_id` FROM `orders` WHERE `fk_user_id` = (SELECT `pk_user_id` FROM `users` WHERE `email` = 'rahul.verma@gmail.com') AND `fk_status_id` = (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'PENDING_PAYMENT') ORDER BY `created_at_utc` DESC LIMIT 1), (SELECT `pk_product_id` FROM `products` WHERE `title` = 'Classic Slim Fit T-Shirt'), 2, 599.00, '2025-12-25 18:30:22'),

-- Pooja's Latest Pending Order Combo (February 2026)
(UUID_V7(), (SELECT `pk_order_id` FROM `orders` WHERE `fk_user_id` = (SELECT `pk_user_id` FROM `users` WHERE `email` = 'pooja.singh@yahoo.com') AND `fk_status_id` = (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'PENDING_PAYMENT') ORDER BY `created_at_utc` DESC LIMIT 1), (SELECT `pk_product_id` FROM `products` WHERE `title` = 'SmartPhone X1'), 1, 29999.00, '2026-02-14 11:20:00'),
(UUID_V7(), (SELECT `pk_order_id` FROM `orders` WHERE `fk_user_id` = (SELECT `pk_user_id` FROM `users` WHERE `email` = 'pooja.singh@yahoo.com') AND `fk_status_id` = (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'PENDING_PAYMENT') ORDER BY `created_at_utc` DESC LIMIT 1), (SELECT `pk_product_id` FROM `products` WHERE `title` = 'Classic Slim Fit T-Shirt'), 1, 599.00, '2026-02-14 11:20:00'),

-- Anita's Latest Pending Order (May 2026)
(UUID_V7(), (SELECT `pk_order_id` FROM `orders` WHERE `fk_user_id` = (SELECT `pk_user_id` FROM `users` WHERE `email` = 'anita@fashionhub.com') AND `fk_status_id` = (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'PENDING_PAYMENT') ORDER BY `created_at_utc` DESC LIMIT 1), (SELECT `pk_product_id` FROM `products` WHERE `title` = 'SmartPhone X1'), 2, 29999.00, '2026-05-20 16:40:00'),

-- Rajesh's Pending Order (June 14, 2026)
(UUID_V7(), (SELECT `pk_order_id` FROM `orders` WHERE `fk_user_id` = (SELECT `pk_user_id` FROM `users` WHERE `email` = 'rajesh@techsolutions.com') AND `fk_status_id` = (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'PENDING_PAYMENT') ORDER BY `created_at_utc` DESC LIMIT 1), (SELECT `pk_product_id` FROM `products` WHERE `title` = 'ProBook 15 Laptop'), 1, 52000.00, '2026-06-14 11:15:00'),
(UUID_V7(), (SELECT `pk_order_id` FROM `orders` WHERE `fk_user_id` = (SELECT `pk_user_id` FROM `users` WHERE `email` = 'rajesh@techsolutions.com') AND `fk_status_id` = (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'PENDING_PAYMENT') ORDER BY `created_at_utc` DESC LIMIT 1), (SELECT `pk_product_id` FROM `products` WHERE `title` = 'SmartPhone X1'), 1, 29999.00, '2026-06-14 11:15:00'),
(UUID_V7(), (SELECT `pk_order_id` FROM `orders` WHERE `fk_user_id` = (SELECT `pk_user_id` FROM `users` WHERE `email` = 'rajesh@techsolutions.com') AND `fk_status_id` = (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'PENDING_PAYMENT') ORDER BY `created_at_utc` DESC LIMIT 1), (SELECT `pk_product_id` FROM `products` WHERE `title` = 'Classic Slim Fit T-Shirt'), 1, 599.00, '2026-06-14 11:15:00');

/*==============================================================
  8. PAYMENT STATUSES
==============================================================*/
INSERT INTO `payment_statuses` (`pk_status_id`, `status_name`, `description`, `created_at_utc`) VALUES
(UUID_V7(), 'PENDING', 'Payment authorization is initiated but incomplete.', '2025-01-01 00:00:00'),
(UUID_V7(), 'SUCCESS', 'Payment successfully processed and captured.', '2025-01-01 00:00:00'),
(UUID_V7(), 'FAILED', 'Payment attempt declined or failed.', '2025-01-01 00:00:00'),
(UUID_V7(), 'REFUNDED', 'Payment amount returned to the buyer.', '2025-01-01 00:00:00');

/*==============================================================
  9. PAYMENTS (Uncommented, fixed, and active!)
==============================================================*/
-- INSERT INTO `payments` (
--     `pk_payment_id`, `fk_order_id`, `payment_method`, `fk_status_id`, 
--     `razorpay_order_id`, `razorpay_payment_id`, `razorpay_payment_link_id`, `razorpay_signature`, `amount_paid`, `paid_at`, `created_at_utc`
-- ) VALUES
-- Success record for Rahul's Completed Order
-- (
--     UUID_V7(), 
--     (SELECT `pk_order_id` FROM `orders` WHERE `fk_user_id` = (SELECT `pk_user_id` FROM `users` WHERE `email` = 'rahul.verma@gmail.com') AND `fk_status_id` = (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'COMPLETED') ORDER BY `created_at_utc` DESC LIMIT 1),
--     'CARD', 
--     (SELECT `pk_status_id` FROM `payment_statuses` WHERE `status_name` = 'SUCCESS'), 
--     'order_T1Ck1ZwOHW9W0T', 'pay_T1ClngVWJ0Dsvn', 'plink_T1Ck2BV0mHgLDs','363f2688fabfa8eae6007111f862ef7c14412167d91a126e8595a6d65239e96e', 52599.00, '2025-04-12 10:20:00', '2025-04-12 10:15:30'
-- ),

-- Initialized/Pending record for Pooja's Oldest Pending Order

-- (
--     UUID_V7(), 
--     (SELECT `pk_order_id` FROM `orders` WHERE `fk_user_id` = (SELECT `pk_user_id` FROM `users` WHERE `email` = 'pooja.singh@yahoo.com') AND `fk_status_id` = (SELECT `pk_status_id` FROM `order_statuses` WHERE `status_name` = 'PENDING_PAYMENT') ORDER BY `created_at_utc` ASC LIMIT 1),
--     'UPI', 
--     (SELECT `pk_status_id` FROM `payment_statuses` WHERE `status_name` = 'PENDING'), 
--     'order_T1CMMufSFJMdub', NULL, NULL, 29999.00, NULL, '2025-07-22 14:45:00'
-- );

/*==============================================================
  10. REVIEWS
==============================================================*/
INSERT INTO `reviews` (`pk_review_id`, `fk_product_id`, `fk_user_id`, `rating`, `review_text`, `created_at_utc`) VALUES
(UUID_V7(), (SELECT `pk_product_id` FROM `products` WHERE `title` = 'ProBook 15 Laptop'), (SELECT `pk_user_id` FROM `users` WHERE `email` = 'rahul.verma@gmail.com'), 5, 'Absolutely loving the processing speed! Well worth the price.', '2025-04-15 12:00:00'),
(UUID_V7(), (SELECT `pk_product_id` FROM `products` WHERE `title` = 'Classic Slim Fit T-Shirt'), (SELECT `pk_user_id` FROM `users` WHERE `email` = 'rahul.verma@gmail.com'), 4, 'Good fit and quality fabric, but delivery took a day extra.', '2025-04-16 14:30:00');