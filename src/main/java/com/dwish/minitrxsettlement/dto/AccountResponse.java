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
public class AccountResponse {
    private UUID id;

    private String name;

    private String cif;

    private BigDecimal balance;

    private Instant createdAt;
}
