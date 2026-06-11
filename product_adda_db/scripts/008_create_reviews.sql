/*
===============================================================================
Table       : reviews
Description :
    Stores product reviews and ratings given by customers.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS reviews
(
    review_id      BIGINT AUTO_INCREMENT NOT NULL,
    product_id     BIGINT                NOT NULL,
    customer_id    BIGINT                NOT NULL,

    rating         INT                   NOT NULL,
    review_text    TEXT                  NULL,

    CONSTRAINT pk_reviews
        PRIMARY KEY (review_id),

    CONSTRAINT fk_reviews_product
        FOREIGN KEY (product_id)
        REFERENCES products(product_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_reviews_customer
        FOREIGN KEY (customer_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_reviews_product_id
    ON reviews(product_id);

CREATE INDEX idx_reviews_customer_id
    ON reviews(customer_id);

CREATE INDEX idx_reviews_rating
    ON reviews(rating);

-- ============================================================================
-- Table Verification
-- ============================================================================

DESCRIBE reviews;

SHOW CREATE TABLE reviews;