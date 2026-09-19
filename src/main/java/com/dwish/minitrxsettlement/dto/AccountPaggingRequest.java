package com.dwish.minitrxsettlement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AccountPaggingRequest(
        @Min(value = 1, message = "Halaman minimal adalah 1")
        int page,

        @Min(value = 1, message = "Ukuran Halaman minimal adalah 1")
        @Max(value = 100, message = "Ukuran halaman maksimal adalah 100")
        int size,

        @NotBlank(message = "direction tidak boleh kosong")
        @Pattern(regexp = "^(?i)(asc|desc)$", message = "Direction harus bernilai 'asc' atau 'desc'")
        String direction
) {
    public AccountPaggingRequest {
        if (direction != null) {
            direction = direction.toLowerCase();
        }
    }
}
