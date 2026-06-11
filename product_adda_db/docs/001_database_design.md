# ProductAdda Database Design Document

## Project Information

Project Name: ProductAdda Marketplace Platform

Database Name: product_adda_db

Version: 1.0

Prepared By: Dheeraj

Reference Documents:

- Business Requirements Document (BRD)
- Software Requirement Specification (SRS)

---

# Database Design Objective

This document defines the initial database structure required for ProductAdda Marketplace Platform.

The design follows the database tables specified in the SRS document.

Only relationships required to support the documented modules are defined at this stage.

Additional schema refinements may be introduced later if required during implementation.

---

# Tables

## USERS

Purpose:

Store platform users.

Columns:

| Column Name | Key Type |
| ----------- | -------- |
| user_id     | PK       |
| first_name  |          |
| last_name   |          |
| email       |          |
| mobile      |          |

Primary Key:

- user_id

---

## VENDORS

Purpose:

Store vendor information.

Columns:

| Column Name   | Key Type |
| ------------- | -------- |
| vendor_id     | PK       |
| user_id       | FK       |
| business_name |          |
| gst_number    |          |

Primary Key:

- vendor_id

Foreign Keys:

- user_id → USERS.user_id

---

## CATEGORIES

Purpose:

Store product categories.

Columns:

| Column Name   | Key Type |
| ------------- | -------- |
| category_id   | PK       |
| category_name |          |

Primary Key:

- category_id

---

## PRODUCTS

Purpose:

Store products listed by vendors.

Columns:

| Column Name    | Key Type |
| -------------- | -------- |
| product_id     | PK       |
| vendor_id      | FK       |
| category_id    | FK       |
| title          |          |
| description    |          |
| price          |          |
| stock_quantity |          |
| payment_id     | FK       |
| order_id       | FK       |

Primary Key:

- product_id

Foreign Keys:

- vendor_id → VENDORS.vendor_id
- category_id → CATEGORIES.category_id
- payment_id → PAYMENTS.transaction_id
- order_id → ORDERS.order_id

---

## ORDERS

Purpose:

Store customer orders.

Columns:

| Column Name  | Key Type |
| ------------ | -------- |
| order_id     | PK       |
| customer_id  | FK       |
| order_status |          |
| total_amount |          |

Primary Key:

- order_id

Foreign Keys:

- customer_id → USERS.user_id

---

## ORDER_ITEMS

Purpose:

Store products associated with orders.

Columns:

| Column Name | Key Type |
| ----------- | -------- |
| item_id     | PK       |
| order_id    | FK       |
| product_id  | FK       |
| quantity    |          |

Primary Key:

- item_id

Foreign Keys:

- order_id → ORDERS.order_id
- product_id → PRODUCTS.product_id

---

## PAYMENTS

Purpose:

Store payment transaction details.

Columns:

| Column Name    | Key Type |
| -------------- | -------- |
| transaction_id | PK       |
| payment_status |          |

Primary Key:

- transaction_id

---

## REVIEWS

Purpose:

Store product reviews and ratings.

Columns:

| Column Name | Key Type |
| ----------- | -------- |
| review_id   | PK       |
| product_id  | FK       |
| customer_id | FK       |
| rating      |          |
| review_text |          |

Primary Key:

- review_id

Foreign Keys:

- product_id → PRODUCTS.product_id
- customer_id → USERS.user_id

---

# Relationship Summary

USERS
|
+-- VENDORS
|
+-- ORDERS
|
+-- REVIEWS

VENDORS
|
+-- PRODUCTS

CATEGORIES
|
+-- PRODUCTS

ORDERS
|
+-- ORDER_ITEMS

PRODUCTS
|
+-- ORDER_ITEMS
|
+-- REVIEWS

PAYMENTS
|
+-- PRODUCTS

---

# Normalization Notes

Current design follows the table structure defined in the SRS.

Primary Keys and Foreign Keys have been identified to establish referential integrity between related tables.

Further normalization review will be performed during implementation if additional business requirements are introduced.
