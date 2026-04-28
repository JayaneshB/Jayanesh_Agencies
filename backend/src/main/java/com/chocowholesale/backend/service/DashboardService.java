package com.chocowholesale.backend.service;

import com.chocowholesale.backend.dto.DashboardSummary;
import com.chocowholesale.backend.entity.OrderStatus;
import com.chocowholesale.backend.repository.InventoryRepository;
import com.chocowholesale.backend.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class DashboardService {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    public DashboardService(OrderRepository orderRepository, InventoryRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public DashboardSummary getSummary() {
        OffsetDateTime todayStart = LocalDate.now().atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime todayEnd = todayStart.plusDays(1);

        var todayRevenue = orderRepository.sumRevenueInRange(todayStart, todayEnd);
        long todayOrders = orderRepository.countOrdersInRange(todayStart, todayEnd);
        long pending = orderRepository.countByStatusIn(List.of(OrderStatus.CONFIRMED, OrderStatus.PROCESSING));
        long outForDelivery = orderRepository.countByStatusIn(List.of(OrderStatus.OUT_FOR_DELIVERY));
        long lowStock = inventoryRepository.countLowStock();

        return new DashboardSummary(todayRevenue, todayOrders, pending, outForDelivery, lowStock);
    }
}
