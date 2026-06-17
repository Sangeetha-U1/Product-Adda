USE product_adda_db;

/*==============================================================
000. TRUNCATE
==============================================================*/

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `reports`;

SET FOREIGN_KEY_CHECKS = 1;

/*==============================================================
 034. SEED DATA FOR: reports
==============================================================*/

INSERT INTO reports (pk_report_id, fk_user_id, fk_report_type_id, report_name, file_url, generated_at_utc, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com'), (SELECT pk_report_type_id FROM report_types WHERE report_type_name = 'SALES'), 'Monthly Sales Report - June 2026', 'https://storage.productadda.com/reports/june-sales-report.pdf', '2026-06-15 10:00:00', '2026-06-15 10:00:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com'), (SELECT pk_report_type_id FROM report_types WHERE report_type_name = 'REVENUE'), 'Quarterly Revenue Report', 'https://storage.productadda.com/reports/quarterly-revenue-report.pdf', '2026-06-15 10:15:00', '2026-06-15 10:15:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'admin@productadda.com'), (SELECT pk_report_type_id FROM report_types WHERE report_type_name = 'VENDOR_PERFORMANCE'), 'Vendor Performance Report', 'https://storage.productadda.com/reports/vendor-performance.pdf', '2026-06-15 10:30:00', '2026-06-15 10:30:00');
