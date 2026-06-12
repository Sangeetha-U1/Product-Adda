/*
===============================================================================
Table       : reviews
Description :
    Stores product reviews and ratings submitted by customers.
===============================================================================
*/

USE product_adda_db;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS reviews
(
    pk_review_id      BINARY(16)        NOT NULL,
    fk_product_id     BINARY(16)        NOT NULL,
    fk_user_id        BINARY(16)        NOT NULL,

    rating            INT               NOT NULL,
    review_text       TEXT              NULL,

    CONSTRAINT pk_reviews_review_id
        PRIMARY KEY (pk_review_id),

    CONSTRAINT fk_reviews_product_id
        FOREIGN KEY (fk_product_id)
        REFERENCES products(pk_product_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_reviews_user_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE CASCADE
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_reviews_product_id
    ON reviews(fk_product_id);

CREATE INDEX idx_reviews_user_id
    ON reviews(fk_user_id);

CREATE INDEX idx_reviews_rating
    ON reviews(rating);

-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE reviews;

SHOW CREATE TABLE reviews;