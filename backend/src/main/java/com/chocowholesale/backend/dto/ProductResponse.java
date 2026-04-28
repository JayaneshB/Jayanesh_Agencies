package com.chocowholesale.backend.dto;

import com.chocowholesale.backend.entity.Product;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String name,
    String description,
    UUID categoryId,
    String categoryName,
    String hsnCode,
    BigDecimal taxRate,
    Boolean isActive,
    InventoryDto inventory,
    List<PricingTierDto> pricingTiers,
    List<ImageDto> images,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public record InventoryDto(Integer totalStock, Integer reserved, Integer available, Integer reorderThreshold, Integer moq) {}
    public record PricingTierDto(UUID id, Integer minQty, Integer maxQty, BigDecimal price) {}
    public record ImageDto(UUID id, String url, Integer position) {}

    public static ProductResponse from(Product p) {
        var inv = p.getInventory();
        return new ProductResponse(
            p.getId(), p.getName(), p.getDescription(),
            p.getCategory() != null ? p.getCategory().getId() : null,
            p.getCategory() != null ? p.getCategory().getName() : null,
            p.getHsnCode(), p.getTaxRate(), p.getIsActive(),
            inv != null ? new InventoryDto(inv.getTotalStock(), inv.getReserved(), inv.getAvailable(), inv.getReorderThreshold(), inv.getMoq()) : null,
            p.getPricingTiers().stream().map(t -> new PricingTierDto(t.getId(), t.getMinQty(), t.getMaxQty(), t.getPrice())).toList(),
            p.getImages().stream().map(i -> new ImageDto(i.getId(), i.getUrl(), i.getPosition())).toList(),
            p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
