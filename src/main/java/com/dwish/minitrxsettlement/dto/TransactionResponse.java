package com.dwish.minitrxsettlement.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TransactionResponse {
    private UUID id;

    private UUID accountId;

    private String type;

    private BigDecimal amount;

    private String status;

    private Instant createdAt;

    private Instant processedAt;
}
