package com.chocowholesale.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductRequest(
    @NotBlank String name,
    String description,
    UUID categoryId,
    String hsnCode,
    BigDecimal taxRate,
    Boolean isActive,
    Integer stock,
    Integer reorderThreshold,
    Integer moq,
    List<PricingTierDto> pricingTiers,
    List<String> imageUrls
) {
    public record PricingTierDto(
        @NotNull Integer minQty,
        Integer maxQty,
        @NotNull BigDecimal price
    ) {}
}
