package com.chocowholesale.backend.dto;

import com.chocowholesale.backend.entity.Order;
import com.chocowholesale.backend.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    UUID userId,
    String customerName,
    String customerPhone,
    OrderStatus status,
    String paymentStatus,
    String paymentId,
    BigDecimal subtotal,
    BigDecimal taxAmount,
    BigDecimal deliveryFee,
    BigDecimal totalAmount,
    List<ItemDto> items,
    List<StatusHistoryDto> statusHistory,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public record ItemDto(UUID id, UUID productId, String productName, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice) {}
    public record StatusHistoryDto(OrderStatus fromStatus, OrderStatus toStatus, String reason, OffsetDateTime createdAt) {}

    public static OrderResponse from(Order o) {
        return new OrderResponse(
            o.getId(),
            o.getUser() != null ? o.getUser().getId() : null,
            o.getUser() != null ? o.getUser().getName() : null,
            o.getUser() != null ? o.getUser().getPhone() : null,
            o.getStatus(), o.getPaymentStatus(), o.getPaymentId(),
            o.getSubtotal(), o.getTaxAmount(), o.getDeliveryFee(), o.getTotalAmount(),
            o.getItems().stream().map(i -> new ItemDto(
                i.getId(),
                i.getProduct() != null ? i.getProduct().getId() : null,
                i.getProduct() != null ? i.getProduct().getName() : null,
                i.getQuantity(), i.getUnitPrice(), i.getTotalPrice()
            )).toList(),
            o.getStatusHistory().stream().map(h -> new StatusHistoryDto(
                h.getFromStatus(), h.getToStatus(), h.getReason(), h.getCreatedAt()
            )).toList(),
            o.getCreatedAt(), o.getUpdatedAt()
        );
    }
}
