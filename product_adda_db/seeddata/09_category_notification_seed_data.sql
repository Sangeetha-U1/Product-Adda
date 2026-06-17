USE product_adda_db;

/*==============================================================
000. TRUNCATE
==============================================================*/

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `notifications`;

SET FOREIGN_KEY_CHECKS = 1;

/*==============================================================
  033. SEED DATA FOR: notifications
==============================================================*/

INSERT INTO notifications (pk_notification_id, fk_user_id, fk_type_id, fk_channel_id, title, message, is_read, sent_at_utc, created_at_utc) VALUES
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), (SELECT pk_notification_type_id FROM notification_types WHERE notification_type_name = 'REGISTRATION'), (SELECT pk_channel_id FROM notification_channels WHERE channel_name = 'EMAIL'), 'Welcome to ProductAdda', 'Your account has been successfully created.', FALSE, '2025-01-10 09:15:00', '2025-01-10 09:15:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), (SELECT pk_notification_type_id FROM notification_types WHERE notification_type_name = 'ORDER_PLACED'), (SELECT pk_channel_id FROM notification_channels WHERE channel_name = 'SMS'), 'Order Confirmed', 'Your order has been placed successfully.', FALSE, '2025-05-01 14:00:00', '2025-05-01 14:00:00'),
(UUID_V7(), (SELECT pk_user_id FROM users WHERE email = 'rahul.verma@gmail.com'), (SELECT pk_notification_type_id FROM notification_types WHERE notification_type_name = 'PAYMENT_SUCCESS'), (SELECT pk_channel_id FROM notification_channels WHERE channel_name = 'PUSH'), 'Payment Successful', 'Your payment has been received.', TRUE, '2025-05-01 14:10:00', '2025-05-01 14:10:00');
