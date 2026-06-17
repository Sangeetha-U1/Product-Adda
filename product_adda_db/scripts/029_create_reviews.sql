/*
===============================================================================
Table       : reviews
Description :
Stores customer product reviews and ratings.
Supports review moderation workflow and product rating management.
===============================================================================
*/

USE product_adda_db;

DROP TABLE IF EXISTS reviews;

-- ============================================================================
-- Create Table
-- ============================================================================

CREATE TABLE reviews
(
    pk_review_id BINARY(16) NOT NULL,

    fk_product_id BINARY(16) NOT NULL,

    fk_user_id BINARY(16) NOT NULL,

    fk_status_id BINARY(16) NOT NULL,

    rating INT NOT NULL,

    review_title VARCHAR(255) NULL,

    review_text TEXT NULL,

    created_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP()),

    updated_at_utc TIMESTAMP NOT NULL
        DEFAULT (UTC_TIMESTAMP())
        ON UPDATE CURRENT_TIMESTAMP,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_reviews_rating_range
        CHECK (rating BETWEEN 1 AND 5),


    CONSTRAINT pk_reviews_review_id
        PRIMARY KEY (pk_review_id),


    CONSTRAINT fk_reviews_product_id
        FOREIGN KEY (fk_product_id)
        REFERENCES products(pk_product_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_reviews_user_id
        FOREIGN KEY (fk_user_id)
        REFERENCES users(pk_user_id)
        ON DELETE RESTRICT,


    CONSTRAINT fk_reviews_status_id
        FOREIGN KEY (fk_status_id)
        REFERENCES review_statuses(pk_status_id)
        ON DELETE RESTRICT,

    CONSTRAINT uq_reviews_product_user
        UNIQUE (fk_product_id, fk_user_id)
);


-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_reviews_product_id
    ON reviews(fk_product_id);


CREATE INDEX idx_reviews_user_id
    ON reviews(fk_user_id);


CREATE INDEX idx_reviews_status_id
    ON reviews(fk_status_id);


CREATE INDEX idx_reviews_rating
    ON reviews(rating);


CREATE INDEX idx_reviews_is_active
    ON reviews(is_active);


-- ============================================================================
-- Verification
-- ============================================================================

DESCRIBE reviews;

SHOW CREATE TABLE reviews;