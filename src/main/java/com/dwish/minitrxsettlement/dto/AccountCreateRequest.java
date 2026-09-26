package com.dwish.minitrxsettlement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AccountCreateRequest(
        @NotBlank(message = "Name tidak boleh kosong")
        String name,

        @Positive(message = "Initial Balance harus lebih besar dari 0")
        BigDecimal initialBalance,

        @NotBlank(message = "CIF tidak boleh kosong")
        String cif
) {}
