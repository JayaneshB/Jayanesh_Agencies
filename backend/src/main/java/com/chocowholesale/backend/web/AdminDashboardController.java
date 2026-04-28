package com.chocowholesale.backend.web;

import com.chocowholesale.backend.dto.DashboardSummary;
import com.chocowholesale.backend.dto.OrderResponse;
import com.chocowholesale.backend.service.DashboardService;
import com.chocowholesale.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final OrderService orderService;

    public AdminDashboardController(DashboardService dashboardService, OrderService orderService) {
        this.dashboardService = dashboardService;
        this.orderService = orderService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummary> summary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/recent-orders")
    public ResponseEntity<List<OrderResponse>> recentOrders() {
        return ResponseEntity.ok(orderService.recentOrders());
    }
}
