package com.productadda.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "reviews")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_review_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkReviewId;

    /*
     * =========================================================
     * RELATIONSHIPS
     * =========================================================
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_product_id", nullable = false)
    private Product fkProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    private User fkUser;

    /*
     * =========================================================
     * REVIEW DATA
     * =========================================================
     */

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;
}