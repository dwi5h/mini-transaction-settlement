package com.dwish.minitrxsettlement.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionCreateRequest(
        @NotBlank(message = "Account Id tidak boleh kosong")
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", message = "Account Id harus bernilai format id valid")
        String accountId,

        @NotNull(message = "Amount tidak boleh kosong")
        @Positive(message = "Amount harus lebih besar dari 0")
        BigDecimal amount,

        @NotBlank(message = "Type transaksi tidak boleh kosong")
        @Pattern(regexp = "^(?i)(DEBIT|CREDIT)$", message = "Type transaksi harus bernilai 'DEBIT' atau 'CREDIT'")
        String type
) {}
