package com.chocowholesale.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StockAdjustRequest(
    @NotNull Integer quantity,
    @NotBlank String reason,
    String note
) {}
