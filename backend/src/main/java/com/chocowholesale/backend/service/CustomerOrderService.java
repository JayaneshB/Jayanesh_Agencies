package com.chocowholesale.backend.service;

import com.chocowholesale.backend.dto.OrderResponse;
import com.chocowholesale.backend.entity.*;
import com.chocowholesale.backend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class CustomerOrderService {

    private final OrderRepository orderRepo;
    private final CartItemRepository cartRepo;
    private final ProductRepository productRepo;
    private final InventoryRepository inventoryRepo;
    private final AddressRepository addressRepo;
    private final UserRepository userRepo;

    public CustomerOrderService(OrderRepository orderRepo, CartItemRepository cartRepo,
                                ProductRepository productRepo, InventoryRepository inventoryRepo,
                                AddressRepository addressRepo, UserRepository userRepo) {
        this.orderRepo = orderRepo;
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.inventoryRepo = inventoryRepo;
        this.addressRepo = addressRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public OrderResponse placeOrder(UUID userId, UUID addressId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address not found"));

        List<CartItem> cartItems = cartRepo.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setDeliveryAddress(address);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setPaymentStatus("PENDING");

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (CartItem ci : cartItems) {
            Product product = ci.getProduct();

            // Stock check
            Inventory inv = inventoryRepo.findByProductId(product.getId()).orElse(null);
            if (inv == null || inv.getAvailable() < ci.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Insufficient stock for: " + product.getName());
            }

            BigDecimal unitPrice = resolvePrice(product, ci.getQuantity());
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));
            BigDecimal lineTax = lineTotal.multiply(product.getTaxRate())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(ci.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setTaxRate(product.getTaxRate());
            item.setTotalPrice(lineTotal.add(lineTax));
            order.getItems().add(item);

            // Reserve stock
            inv.setReserved(inv.getReserved() + ci.getQuantity());
            inventoryRepo.save(inv);

            subtotal = subtotal.add(lineTotal);
            totalTax = totalTax.add(lineTax);
        }

        BigDecimal deliveryFee = subtotal.compareTo(BigDecimal.valueOf(2000)) >= 0
                ? BigDecimal.ZERO : BigDecimal.valueOf(50);

        order.setSubtotal(subtotal);
        order.setTaxAmount(totalTax);
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(subtotal.add(totalTax).add(deliveryFee));

        OrderStatusHistory hist = new OrderStatusHistory();
        hist.setOrder(order);
        hist.setToStatus(OrderStatus.PENDING_PAYMENT);
        hist.setActor(user);
        order.getStatusHistory().add(hist);

        order = orderRepo.save(order);

        // Clear cart
        cartRepo.deleteByUserId(userId);

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> myOrders(UUID userId, Pageable pageable) {
        return orderRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse myOrder(UUID userId, UUID orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your order");
        }
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse verifyPayment(UUID userId, UUID orderId, String paymentId, String signature) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is not pending payment");
        }

        // TODO: Verify Razorpay signature server-side
        order.setPaymentId(paymentId);
        order.setPaymentStatus("PAID");
        order.setStatus(OrderStatus.CONFIRMED);

        OrderStatusHistory hist = new OrderStatusHistory();
        hist.setOrder(order);
        hist.setFromStatus(OrderStatus.PENDING_PAYMENT);
        hist.setToStatus(OrderStatus.CONFIRMED);
        hist.setReason("Payment verified");
        order.getStatusHistory().add(hist);

        orderRepo.save(order);
        return OrderResponse.from(order);
    }

    private BigDecimal resolvePrice(Product product, int qty) {
        List<PricingTier> tiers = product.getPricingTiers();
        if (tiers == null || tiers.isEmpty()) return BigDecimal.ZERO;
        tiers.sort(Comparator.comparingInt(PricingTier::getMinQty));
        BigDecimal price = tiers.get(0).getPrice();
        for (PricingTier t : tiers) {
            if (qty >= t.getMinQty()) price = t.getPrice();
        }
        return price;
    }
}
