package com.chocowholesale.backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(List<Item> items, BigDecimal subtotal, int totalItems) {
    public record Item(
        UUID productId, String productName, String imageUrl,
        Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal,
        Integer availableStock
    ) {}
}
