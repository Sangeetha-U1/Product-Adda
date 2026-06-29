USE product_adda_db;

/*==============================================================
000. TRUNCATE
==============================================================*/

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `inventory_transactions`;
TRUNCATE TABLE `inventory`;

SET FOREIGN_KEY_CHECKS = 1;

/*==============================================================
020. SEED DATA FOR: inventory
==============================================================*/

INSERT INTO inventory (pk_inventory_id, fk_product_id, available_quantity, reserved_quantity, low_stock_threshold,max_stock, is_active, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_product_id FROM products WHERE sku = 'MOB-SAMSUNG-S24-001'), 100, 5, 10, 500, TRUE, '2025-03-10 10:10:00'),
(UUID_V7(), (SELECT pk_product_id FROM products WHERE sku = 'LAP-DELL-XPS15-001'), 50, 2, 10, 600, TRUE, '2025-07-22 14:45:00'),
(UUID_V7(), (SELECT pk_product_id FROM products WHERE sku = 'MEN-NIKE-TSHIRT-001'), 200, 10, 20, 800, TRUE, '2025-11-05 09:30:00');

/*==============================================================
021. SEED DATA FOR: inventory_transactions
==============================================================*/

INSERT INTO inventory_transactions (pk_inventory_transaction_id, fk_product_id, fk_transaction_type_id, quantity, reference_type, reference_id, remarks, is_active, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_product_id FROM products WHERE sku = 'MOB-SAMSUNG-S24-001'), (SELECT pk_transaction_type_id FROM inventory_transaction_types WHERE transaction_type_name = 'STOCK_IN'), 100, 'INITIAL_STOCK', NULL, 'Initial inventory load', TRUE, '2025-03-10 10:15:00'),
(UUID_V7(), (SELECT pk_product_id FROM products WHERE sku = 'LAP-DELL-XPS15-001'), (SELECT pk_transaction_type_id FROM inventory_transaction_types WHERE transaction_type_name = 'STOCK_IN'), 50, 'INITIAL_STOCK', NULL, 'Initial inventory load', TRUE, '2025-07-22 14:50:00'),
(UUID_V7(), (SELECT pk_product_id FROM products WHERE sku = 'MEN-NIKE-TSHIRT-001'), (SELECT pk_transaction_type_id FROM inventory_transaction_types WHERE transaction_type_name = 'STOCK_IN'), 200, 'INITIAL_STOCK', NULL, 'Initial inventory load', TRUE, '2025-11-05 09:35:00');
