package com.dwish.minitrxsettlement.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "transaction")
@NoArgsConstructor()
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID accountId;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;
}
