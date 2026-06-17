USE product_adda_db;

/*==============================================================
000. TRUNCATE
==============================================================*/

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `product_images`;
TRUNCATE TABLE `products`;
TRUNCATE TABLE `categories`;
TRUNCATE TABLE `brands`;

SET FOREIGN_KEY_CHECKS = 1;

/*==============================================================
016. SEED DATA FOR: brands
==============================================================*/

INSERT INTO brands (pk_brand_id, brand_name, brand_description, is_active, created_at_utc) VALUES
(UUID_V7(), 'Apple', 'Consumer electronics and technology products', TRUE, '2024-01-10 09:00:00'),
(UUID_V7(), 'Samsung', 'Electronics, mobile devices and appliances', TRUE, '2024-01-12 10:15:00'),
(UUID_V7(), 'Sony', 'Consumer electronics and entertainment products', TRUE, '2024-01-15 14:30:00'),
(UUID_V7(), 'LG', 'Home appliances and electronics manufacturer', TRUE, '2024-02-02 08:45:00'),
(UUID_V7(), 'Dell', 'Computers, laptops and accessories', TRUE, '2024-02-18 11:00:00'),
(UUID_V7(), 'HP', 'Computers, printers and accessories', TRUE, '2024-03-05 16:20:00'),
(UUID_V7(), 'Lenovo', 'Laptops, desktops and technology products', TRUE, '2024-03-22 13:10:00'),
(UUID_V7(), 'Nike', 'Sportswear and athletic products', TRUE, '2024-04-12 09:05:00'),
(UUID_V7(), 'Adidas', 'Sports apparel and footwear', TRUE, '2024-04-19 10:50:00'),
(UUID_V7(), 'Puma', 'Sports and lifestyle products', TRUE, '2024-05-01 15:35:00');

/*==============================================================
017. SEED DATA FOR: categories
==============================================================*/

INSERT INTO categories (pk_category_id, fk_parent_category_id, category_name, category_description, display_order, is_active, created_at_utc) VALUES
(UUID_V7(), NULL, 'Electronics', 'Electronic devices and accessories', 1, TRUE, '2024-06-01 08:00:00'),
(UUID_V7(), NULL, 'Fashion', 'Fashion and lifestyle products', 2, TRUE, '2024-06-02 09:00:00'),
(UUID_V7(), NULL, 'Home & Kitchen', 'Home and kitchen essentials', 3, TRUE, '2024-06-03 10:00:00'),
(UUID_V7(), NULL, 'Books', 'Books and educational materials', 4, TRUE, '2024-06-04 11:00:00'),
(UUID_V7(), NULL, 'Sports', 'Sports and fitness products', 5, TRUE, '2024-06-05 12:00:00'),
(UUID_V7(), NULL, 'Beauty', 'Beauty and personal care products', 6, TRUE, '2024-06-06 13:00:00');

/*==============================================================
017A. CHILD CATEGORY SEED DATA
==============================================================*/

INSERT INTO categories (pk_category_id, fk_parent_category_id, category_name, category_description, display_order, is_active, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_category_id FROM (SELECT pk_category_id FROM categories WHERE category_name = 'Electronics') as temp), 'Mobile Phones', 'Smartphones and accessories', 10, TRUE, '2024-06-15 09:00:00'),
(UUID_V7(), (SELECT pk_category_id FROM (SELECT pk_category_id FROM categories WHERE category_name = 'Electronics') as temp), 'Laptops', 'Laptops and notebooks', 11, TRUE, '2024-06-15 10:30:00'),
(UUID_V7(), (SELECT pk_category_id FROM (SELECT pk_category_id FROM categories WHERE category_name = 'Electronics') as temp), 'Televisions', 'Smart TVs and accessories', 12, TRUE, '2024-06-15 14:15:00'),
(UUID_V7(), (SELECT pk_category_id FROM (SELECT pk_category_id FROM categories WHERE category_name = 'Fashion') as temp), 'Men Clothing', 'Fashion products for men', 20, TRUE, '2024-06-16 11:00:00'),
(UUID_V7(), (SELECT pk_category_id FROM (SELECT pk_category_id FROM categories WHERE category_name = 'Fashion') as temp), 'Women Clothing', 'Fashion products for women', 21, TRUE, '2024-06-16 15:45:00'),
(UUID_V7(), (SELECT pk_category_id FROM (SELECT pk_category_id FROM categories WHERE category_name = 'Home & Kitchen') as temp), 'Kitchen Appliances', 'Kitchen appliances and accessories', 30, TRUE, '2024-06-17 10:00:00'),
(UUID_V7(), (SELECT pk_category_id FROM (SELECT pk_category_id FROM categories WHERE category_name = 'Sports') as temp), 'Fitness Equipment', 'Fitness and gym equipment', 40, TRUE, '2024-06-18 16:20:00');

/*==============================================================
018. SEED DATA FOR: products
==============================================================*/

INSERT INTO products (pk_product_id, fk_vendor_id, fk_category_id, fk_brand_id, title, description, sku, price, discount_price, stock_quantity, average_rating, total_reviews, is_active, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_vendor_id FROM vendors WHERE store_name = 'Tech Solutions Store'), (SELECT pk_category_id FROM categories WHERE category_name = 'Mobile Phones'), (SELECT pk_brand_id FROM brands WHERE brand_name = 'Samsung'), 'Samsung Galaxy S24 Ultra', 'Samsung flagship smartphone', 'MOB-SAMSUNG-S24-001', 129999.00, 124999.00, 50, 4.80, 125, TRUE, '2025-03-10 10:00:00'),
(UUID_V7(), (SELECT pk_vendor_id FROM vendors WHERE store_name = 'Tech Solutions Store'), (SELECT pk_category_id FROM categories WHERE category_name = 'Laptops'), (SELECT pk_brand_id FROM brands WHERE brand_name = 'Dell'), 'Dell XPS 15', 'Premium performance laptop', 'LAP-DELL-XPS15-001', 159999.00, 149999.00, 20, 4.70, 84, TRUE, '2025-07-22 14:30:00'),
(UUID_V7(), (SELECT pk_vendor_id FROM vendors WHERE store_name = 'Fashion Hub Store'), (SELECT pk_category_id FROM categories WHERE category_name = 'Men Clothing'), (SELECT pk_brand_id FROM brands WHERE brand_name = 'Nike'), 'Nike Sports T-Shirt', 'Breathable sports t-shirt', 'MEN-NIKE-TSHIRT-001', 1999.00, 1499.00, 100, 4.50, 43, TRUE, '2025-11-05 09:15:00');

/*==============================================================
019. SEED DATA FOR: product_images
==============================================================*/

INSERT INTO product_images (pk_product_image_id, fk_product_id, image_url, file_name, mime_type, file_size_bytes, width_pixels, height_pixels, alt_text, is_primary, display_order, is_active, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_product_id FROM products WHERE sku = 'MOB-SAMSUNG-S24-001'), 'https://cdn.productadda.com/products/s24-front.jpg', 's24-front.jpg', 'image/jpeg', 524288, 1440, 3120, 'Samsung Galaxy S24 Ultra Front View', TRUE, 1, TRUE, '2025-03-10 10:05:00'),
(UUID_V7(), (SELECT pk_product_id FROM products WHERE sku = 'LAP-DELL-XPS15-001'), 'https://cdn.productadda.com/products/dell-xps15.jpg', 'dell-xps15.jpg', 'image/jpeg', 412568, 1920, 1080, 'Dell XPS 15 Laptop', TRUE, 1, TRUE, '2025-07-22 14:40:00'),
(UUID_V7(), (SELECT pk_product_id FROM products WHERE sku = 'MEN-NIKE-TSHIRT-001'), 'https://cdn.productadda.com/products/nike-tshirt.jpg', 'nike-tshirt.jpg', 'image/jpeg', 205684, 1200, 1200, 'Nike Sports T-Shirt', TRUE, 1, TRUE, '2025-11-05 09:25:00');
