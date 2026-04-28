package com.chocowholesale.backend.dto;

import com.chocowholesale.backend.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
    @NotNull OrderStatus status,
    String reason
) {}
