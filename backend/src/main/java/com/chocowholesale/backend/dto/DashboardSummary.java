package com.chocowholesale.backend.dto;

import java.math.BigDecimal;

public record DashboardSummary(
    BigDecimal todayRevenue,
    long todayOrderCount,
    long pendingOrders,
    long outForDeliveryCount,
    long lowStockSkuCount
) {}
