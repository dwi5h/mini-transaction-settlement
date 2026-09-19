package com.dwish.minitrxsettlement.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@Table(name = "transaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID accountId;

    private String type;

    private BigDecimal amount;

    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;
}
