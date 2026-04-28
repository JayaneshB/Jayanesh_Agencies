package com.chocowholesale.backend.service;

import com.chocowholesale.backend.dto.CartItemDto;
import com.chocowholesale.backend.dto.CartResponse;
import com.chocowholesale.backend.entity.CartItem;
import com.chocowholesale.backend.entity.PricingTier;
import com.chocowholesale.backend.entity.Product;
import com.chocowholesale.backend.entity.User;
import com.chocowholesale.backend.repository.CartItemRepository;
import com.chocowholesale.backend.repository.ProductRepository;
import com.chocowholesale.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CartService {

    private final CartItemRepository cartRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    public CartService(CartItemRepository cartRepo, ProductRepository productRepo, UserRepository userRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        List<CartItem> items = cartRepo.findByUserId(userId);
        BigDecimal subtotal = BigDecimal.ZERO;
        List<CartResponse.Item> responseItems = new ArrayList<>();

        for (CartItem ci : items) {
            Product p = ci.getProduct();
            BigDecimal unitPrice = resolvePrice(p, ci.getQuantity());
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            String imageUrl = p.getImages().isEmpty() ? null : p.getImages().get(0).getUrl();
            int available = p.getInventory() != null ? p.getInventory().getAvailable() : 0;

            responseItems.add(new CartResponse.Item(
                p.getId(), p.getName(), imageUrl,
                ci.getQuantity(), unitPrice, lineTotal, available
            ));
        }

        return new CartResponse(responseItems, subtotal, responseItems.size());
    }

    @Transactional
    public CartResponse syncCart(UUID userId, List<CartItemDto> items) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        cartRepo.deleteByUserId(userId);

        for (CartItemDto dto : items) {
            if (dto.quantity() <= 0) continue;
            Product product = productRepo.findById(dto.productId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found: " + dto.productId()));
            CartItem ci = new CartItem();
            ci.setUser(user);
            ci.setProduct(product);
            ci.setQuantity(dto.quantity());
            cartRepo.save(ci);
        }

        return getCart(userId);
    }

    @Transactional
    public CartResponse addItem(UUID userId, UUID productId, int quantity) {
        User user = userRepo.findById(userId).orElseThrow();
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Optional<CartItem> existing = cartRepo.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            CartItem ci = existing.get();
            ci.setQuantity(ci.getQuantity() + quantity);
            if (ci.getQuantity() <= 0) {
                cartRepo.delete(ci);
            } else {
                cartRepo.save(ci);
            }
        } else if (quantity > 0) {
            CartItem ci = new CartItem();
            ci.setUser(user);
            ci.setProduct(product);
            ci.setQuantity(quantity);
            cartRepo.save(ci);
        }

        return getCart(userId);
    }

    @Transactional
    public CartResponse removeItem(UUID userId, UUID productId) {
        cartRepo.deleteByUserIdAndProductId(userId, productId);
        return getCart(userId);
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
