package com.dwish.minitrxsettlement.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionCreateRequest(
        @NotBlank(message = "Account Id tidak boleh kosong")
        UUID accountId,

        @NotNull(message = "Amount tidak boleh kosong")
        @Positive(message = "Amount harus lebih besar dari 0")
        BigDecimal amount,

        @NotBlank(message = "Type transaksi tidak boleh kosong")
        String type
) {}
