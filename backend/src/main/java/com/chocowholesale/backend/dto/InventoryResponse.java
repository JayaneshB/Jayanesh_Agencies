package com.chocowholesale.backend.dto;

import com.chocowholesale.backend.entity.Inventory;

import java.util.UUID;

public record InventoryResponse(
    UUID id,
    UUID productId,
    String productName,
    Integer totalStock,
    Integer reserved,
    Integer sold,
    Integer available,
    Integer reorderThreshold,
    Integer moq
) {
    public static InventoryResponse from(Inventory i) {
        return new InventoryResponse(
            i.getId(), i.getProduct().getId(), i.getProduct().getName(),
            i.getTotalStock(), i.getReserved(), i.getSold(), i.getAvailable(),
            i.getReorderThreshold(), i.getMoq()
        );
    }
}
