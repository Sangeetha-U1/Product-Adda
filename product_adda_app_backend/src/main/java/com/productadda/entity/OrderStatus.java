package com.productadda.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "order_statuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatus {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_status_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkStatusId;

    @Column(name = "status_name", nullable = false, unique = true, length = 50)
    private String statusName;

    @Column(name = "description", length = 255)
    private String description;

    /*
     * ===========================================================================
     * AUDIT
     * ===========================================================================
     */

    @Column(name = "created_at_utc", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAtUtc;
}