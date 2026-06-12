package com.productadda.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "categories")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_category_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkCategoryId;

    /*
     * =========================================================
     * CATEGORY INFO
     * =========================================================
     */
    @Column(name = "category_name", nullable = false, unique = true, length = 150)
    private String categoryName;
}