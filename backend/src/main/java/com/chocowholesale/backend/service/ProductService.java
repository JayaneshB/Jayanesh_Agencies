package com.chocowholesale.backend.service;

import com.chocowholesale.backend.dto.ProductRequest;
import com.chocowholesale.backend.dto.ProductResponse;
import com.chocowholesale.backend.entity.*;
import com.chocowholesale.backend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listProducts(String search, UUID categoryId, Pageable pageable) {
        Page<Product> page;
        if (search != null && !search.isBlank()) {
            page = productRepository.searchByName(search.trim(), pageable);
        } else if (categoryId != null) {
            page = productRepository.findByCategoryId(categoryId, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }
        return page.map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return ProductResponse.from(p);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest req) {
        Product product = new Product();
        applyFields(product, req);
        product = productRepository.save(product);

        Inventory inv = new Inventory();
        inv.setProduct(product);
        inv.setTotalStock(req.stock() != null ? req.stock() : 0);
        inv.setReorderThreshold(req.reorderThreshold() != null ? req.reorderThreshold() : 0);
        inv.setMoq(req.moq() != null ? req.moq() : 1);
        inventoryRepository.save(inv);

        return ProductResponse.from(productRepository.findById(product.getId()).orElseThrow());
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        applyFields(product, req);

        product.getPricingTiers().clear();
        product.getImages().clear();
        productRepository.saveAndFlush(product);

        addTiersAndImages(product, req);
        product = productRepository.save(product);

        if (product.getInventory() != null) {
            Inventory inv = product.getInventory();
            if (req.stock() != null) inv.setTotalStock(req.stock());
            if (req.reorderThreshold() != null) inv.setReorderThreshold(req.reorderThreshold());
            if (req.moq() != null) inv.setMoq(req.moq());
            inventoryRepository.save(inv);
        }

        return ProductResponse.from(productRepository.findById(id).orElseThrow());
    }

    @Transactional
    public void deactivateProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        product.setIsActive(false);
        productRepository.save(product);
    }

    private void applyFields(Product product, ProductRequest req) {
        product.setName(req.name());
        product.setDescription(req.description());
        product.setHsnCode(req.hsnCode());
        if (req.taxRate() != null) product.setTaxRate(req.taxRate());
        if (req.isActive() != null) product.setIsActive(req.isActive());

        if (req.categoryId() != null) {
            Category cat = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
            product.setCategory(cat);
        }

        addTiersAndImages(product, req);
    }

    private void addTiersAndImages(Product product, ProductRequest req) {
        if (req.pricingTiers() != null) {
            for (var t : req.pricingTiers()) {
                PricingTier tier = new PricingTier();
                tier.setProduct(product);
                tier.setMinQty(t.minQty());
                tier.setMaxQty(t.maxQty());
                tier.setPrice(t.price());
                product.getPricingTiers().add(tier);
            }
        }
        if (req.imageUrls() != null) {
            int pos = 0;
            for (String url : req.imageUrls()) {
                ProductImage img = new ProductImage();
                img.setProduct(product);
                img.setUrl(url);
                img.setPosition(pos++);
                product.getImages().add(img);
            }
        }
    }
}
