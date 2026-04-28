package com.chocowholesale.backend.entity;

public enum OrderStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    PROCESSING,
    PACKED,
    SHIPPED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
