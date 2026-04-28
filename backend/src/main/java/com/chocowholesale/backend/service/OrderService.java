package com.chocowholesale.backend.service;

import com.chocowholesale.backend.dto.OrderResponse;
import com.chocowholesale.backend.dto.StatusUpdateRequest;
import com.chocowholesale.backend.entity.*;
import com.chocowholesale.backend.repository.OrderRepository;
import com.chocowholesale.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS;

    static {
        ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);
        ALLOWED_TRANSITIONS.put(OrderStatus.PENDING_PAYMENT, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.CONFIRMED, Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.PROCESSING, Set.of(OrderStatus.PACKED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.PACKED, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.SHIPPED, Set.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.DELIVERED, Collections.emptySet());
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, Collections.emptySet());
    }

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrders(OrderStatus status, Pageable pageable) {
        Page<Order> page;
        if (status != null) {
            page = orderRepository.findByStatus(status, pageable);
        } else {
            page = orderRepository.findAll(pageable);
        }
        return page.map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse updateStatus(UUID orderId, StatusUpdateRequest req, UUID actorUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        OrderStatus current = order.getStatus();
        OrderStatus next = req.status();

        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Collections.emptySet());
        if (!allowed.contains(next)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot transition from " + current + " to " + next);
        }

        if (next == OrderStatus.CANCELLED && isPostShipped(current) && (req.reason() == null || req.reason().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cancellation after SHIPPED requires a reason");
        }

        User actor = userRepository.findById(actorUserId).orElse(null);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setFromStatus(current);
        history.setToStatus(next);
        history.setActor(actor);
        history.setReason(req.reason());
        order.getStatusHistory().add(history);

        order.setStatus(next);
        orderRepository.save(order);

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> recentOrders() {
        return orderRepository.findTop20ByOrderByCreatedAtDesc()
                .stream().map(OrderResponse::from).toList();
    }

    private boolean isPostShipped(OrderStatus status) {
        return status == OrderStatus.SHIPPED || status == OrderStatus.OUT_FOR_DELIVERY;
    }
}
