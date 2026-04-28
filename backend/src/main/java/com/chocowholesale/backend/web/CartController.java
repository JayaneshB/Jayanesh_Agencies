package com.chocowholesale.backend.web;

import com.chocowholesale.backend.dto.CartItemDto;
import com.chocowholesale.backend.dto.CartResponse;
import com.chocowholesale.backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PutMapping
    public ResponseEntity<CartResponse> syncCart(
            @AuthenticationPrincipal UUID userId, @RequestBody List<CartItemDto> items) {
        return ResponseEntity.ok(cartService.syncCart(userId, items));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal UUID userId, @RequestBody CartItemDto item) {
        return ResponseEntity.ok(cartService.addItem(userId, item.productId(), item.quantity()));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID productId) {
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }
}
