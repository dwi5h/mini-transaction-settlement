package com.dwish.minitrxsettlement.dto;

import jakarta.validation.constraints.NotBlank;

public record AccountCreateRequest(
        @NotBlank(message = "Name tidak boleh kosong")
        String name,

        @NotBlank(message = "CIF tidak boleh kosong")
        String cif
) {}
