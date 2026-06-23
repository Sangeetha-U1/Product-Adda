USE product_adda_db;

/*==============================================================
000. TRUNCATE
==============================================================*/

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `addresses`;
TRUNCATE TABLE `vendor_bank_details`;
TRUNCATE TABLE `vendors`;
TRUNCATE TABLE `user_roles`;
TRUNCATE TABLE `users`;

SET FOREIGN_KEY_CHECKS = 1;

/*==============================================================
011. SEED DATA FOR: users
--------------------------------------------------------------
PLAIN TEXT PASSWORDS FOR TESTING

superadmin@productadda.com --> SuperAdmin@123
admin@productadda.com      --> Admin@123

rajesh@techsolutions.com   --> Vendor@123
anita@fashionhub.com       --> Vendor@123

rahul.verma@gmail.com      --> Customer@123 to new Customer123
pooja.singh@yahoo.com      --> Customer@123

vikram.m@gmail.com         --> User@123
==============================================================*/

INSERT INTO users (pk_user_id, first_name, last_name, email, mobile, password_hash, google_id, email_verified, last_login_at_utc, is_active, created_at_utc) VALUES
(UUID_V7(), 'Varun', 'Sharma', 'superadmin@productadda.com', '9377843209', '$2a$10$TyG6KONDY7vEatUM0b4wKOWbSsjdhzTQ7KwokqPhfFAypCGv1LHTK', NULL, TRUE, '2025-01-12 12:35:00', TRUE, '2025-01-12 12:30:00'),
(UUID_V7(), 'Amit', 'Sharma', 'admin@productadda.com', '9876543210', '$2a$10$1j3ofRgdrvWDhhkPVe7jT.lnNmrzVPQ6bk9mYwr11fC0jR20paISm', NULL, TRUE, '2025-01-15 08:35:00', TRUE, '2025-01-15 08:30:00'),
(UUID_V7(), 'Rajesh', 'Kumar', 'rajesh@techsolutions.com', '9876543211', '$2a$10$7Xxl52l8ZVhs7rgA4ukpCuMjgDLCE0tz4Ufg55cuapaq5PgZNn2GK', NULL, TRUE, '2025-01-20 10:20:00', TRUE, '2025-01-20 10:15:00'),
(UUID_V7(), 'Anita', 'Desai', 'anita@fashionhub.com', '9876543212', '$2a$10$9ckr2EjB0KEOHjohrz24S.HkZcCKRn7LKuHE/SSNt6UHUXFSnDEn6', NULL, TRUE, '2025-02-02 11:50:00', TRUE, '2025-02-02 11:45:00'),
(UUID_V7(), 'Rahul', 'Verma', 'rahul.verma@gmail.com', '9876543213', '$2a$10$MkIcjobv7RhNDYUweNAOs.Pze8.VT6ww9T8kYEhgWrOTQivhBKHvK', NULL, TRUE, '2025-02-18 14:25:00', TRUE, '2025-02-18 14:20:00'),
(UUID_V7(), 'Pooja', 'Singh', 'pooja.singh@yahoo.com', '9876543214', '$2a$10$wWtVDkKKXm1tlQhjHY9xRu1d69R9JtkKgbExoiWPoShvqjLWxEq2S', NULL, TRUE, '2025-03-05 16:15:00', TRUE, '2025-03-05 16:10:00'),
(UUID_V7(), 'Vikram', 'Malhotra', 'vikram.m@gmail.com', '9876543215', '$2a$10$vyGDa4ZUqhK3dzHH9437ROuwZLBNpSsZZWcvuldj7a30U1ZKA/rfW', NULL, TRUE, '2025-03-10 09:05:00', TRUE, '2025-03-10 09:00:00');

/*==============================================================
012. SEED DATA FOR: user_roles
==============================================================*/

INSERT INTO user_roles (pk_user_role_id, fk_user_id, fk_role_id, is_active, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'superadmin@productadda.com'), (SELECT pk_role_id FROM roles WHERE role_name = 'SUPER_ADMIN'), TRUE, '2025-01-12 12:35:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com'), (SELECT pk_role_id FROM roles WHERE role_name = 'ADMIN'), TRUE, '2025-01-15 08:35:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rajesh@techsolutions.com'), (SELECT pk_role_id FROM roles WHERE role_name = 'VENDOR'), TRUE, '2025-01-20 10:20:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'anita@fashionhub.com'), (SELECT pk_role_id FROM roles WHERE role_name = 'VENDOR'), TRUE, '2025-02-02 11:50:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), (SELECT pk_role_id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, '2025-02-18 14:25:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com'), (SELECT pk_role_id FROM roles WHERE role_name = 'CUSTOMER'), TRUE, '2025-03-05 16:15:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'vikram.m@gmail.com'), (SELECT pk_role_id FROM roles WHERE role_name = 'USER'), TRUE, '2025-03-10 09:05:00');

/*==============================================================
013. SEED DATA FOR: vendors
==============================================================*/

INSERT INTO vendors (pk_vendor_id, fk_user_id, business_name, store_name, gst_number, business_description, is_active, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rajesh@techsolutions.com'), 'Tech Solutions Private Limited', 'Tech Solutions Store', '29ABCDE1234F1Z5', 'Electronics and gadgets seller', TRUE, '2025-01-21 09:00:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'anita@fashionhub.com'), 'Fashion Hub Private Limited', 'Fashion Hub Store', '36FGHIJ5678K1Z2', 'Fashion and lifestyle products seller', TRUE, '2025-02-03 10:00:00');

/*==============================================================
014. SEED DATA FOR: vendor_bank_details
==============================================================*/

INSERT INTO vendor_bank_details (pk_vendor_bank_detail_id, fk_vendor_id, account_holder_name, bank_name, account_number, ifsc_code, branch_name, is_active, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_vendor_id FROM vendors WHERE store_name = 'Tech Solutions Store'), 'Rajesh Kumar', 'HDFC Bank', '50100123456789', 'HDFC0001234', 'Madhapur Branch', TRUE, '2025-01-21 09:15:00'),
(UUID_V7(), (SELECT pk_vendor_id FROM vendors WHERE store_name = 'Fashion Hub Store'), 'Anita Desai', 'ICICI Bank', '70100123456789', 'ICIC0005678', 'Banjara Hills Branch', TRUE, '2025-02-03 10:15:00');

/*==============================================================
015. SEED DATA FOR: addresses
==============================================================*/

INSERT INTO addresses (pk_address_id, fk_user_id, fk_address_type_id, address_line_1, address_line_2, landmark, city, state, postal_code, country, is_default, is_active, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'superadmin@productadda.com'), (SELECT pk_address_type_id FROM address_types WHERE address_type_name = 'HOME'), 'Flat 101', 'Green Residency', 'Near City Mall', 'Hyderabad', 'Telangana', '500081', 'India', TRUE, TRUE, '2025-01-12 13:00:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com'), (SELECT pk_address_type_id FROM address_types WHERE address_type_name = 'HOME'), 'Flat 202', 'Sai Enclave', 'Near Metro Station', 'Hyderabad', 'Telangana', '500082', 'India', TRUE, TRUE, '2025-01-15 09:00:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rajesh@techsolutions.com'), (SELECT pk_address_type_id FROM address_types WHERE address_type_name = 'BUSINESS'), 'Plot 12', 'Tech Park', 'HITEC City', 'Hyderabad', 'Telangana', '500084', 'India', TRUE, TRUE, '2025-01-21 09:30:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'anita@fashionhub.com'), (SELECT pk_address_type_id FROM address_types WHERE address_type_name = 'BUSINESS'), 'Road No 36', 'Jubilee Hills', 'Apollo Hospital', 'Hyderabad', 'Telangana', '500033', 'India', TRUE, TRUE, '2025-02-03 10:30:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), (SELECT pk_address_type_id FROM address_types WHERE address_type_name = 'SHIPPING'), 'Flat 301', 'Lake View Apartments', 'Near School', 'Bengaluru', 'Karnataka', '560001', 'India', TRUE, TRUE, '2025-02-18 15:00:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'pooja.singh@yahoo.com'), (SELECT pk_address_type_id FROM address_types WHERE address_type_name = 'SHIPPING'), 'House No 45', 'Palm Meadows', 'Near Park', 'Pune', 'Maharashtra', '411001', 'India', TRUE, TRUE, '2025-03-05 17:00:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'vikram.m@gmail.com'), (SELECT pk_address_type_id FROM address_types WHERE address_type_name = 'HOME'), 'Flat 404', 'Sunshine Residency', 'Near Temple', 'Chennai', 'Tamil Nadu', '600001', 'India', TRUE, TRUE, '2025-03-10 10:00:00');

