package com.chocowholesale.backend.dto;

import java.util.UUID;

public record CartItemDto(UUID productId, Integer quantity) {}
