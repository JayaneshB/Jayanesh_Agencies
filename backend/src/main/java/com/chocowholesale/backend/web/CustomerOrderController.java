package com.chocowholesale.backend.web;

import com.chocowholesale.backend.dto.OrderResponse;
import com.chocowholesale.backend.dto.PlaceOrderRequest;
import com.chocowholesale.backend.service.CustomerOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class CustomerOrderController {

    private final CustomerOrderService orderService;

    public CustomerOrderController(CustomerOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @AuthenticationPrincipal UUID userId, @RequestBody PlaceOrderRequest req) {
        return ResponseEntity.ok(orderService.placeOrder(userId, req.addressId()));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> myOrders(
            @AuthenticationPrincipal UUID userId, Pageable pageable) {
        return ResponseEntity.ok(orderService.myOrders(userId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> myOrder(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return ResponseEntity.ok(orderService.myOrder(userId, id));
    }

    @PostMapping("/{id}/verify-payment")
    public ResponseEntity<OrderResponse> verifyPayment(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(orderService.verifyPayment(
                userId, id, body.get("paymentId"), body.get("signature")));
    }
}
