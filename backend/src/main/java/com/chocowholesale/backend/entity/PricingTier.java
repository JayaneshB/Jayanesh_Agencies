package com.chocowholesale.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pricing_tiers")
public class PricingTier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "min_qty", nullable = false)
    private Integer minQty;

    @Column(name = "max_qty")
    private Integer maxQty;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = OffsetDateTime.now(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Integer getMinQty() { return minQty; }
    public void setMinQty(Integer minQty) { this.minQty = minQty; }
    public Integer getMaxQty() { return maxQty; }
    public void setMaxQty(Integer maxQty) { this.maxQty = maxQty; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
