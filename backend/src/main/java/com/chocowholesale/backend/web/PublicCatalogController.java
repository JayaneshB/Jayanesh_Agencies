package com.chocowholesale.backend.web;

import com.chocowholesale.backend.dto.ProductResponse;
import com.chocowholesale.backend.entity.Category;
import com.chocowholesale.backend.repository.CategoryRepository;
import com.chocowholesale.backend.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PublicCatalogController {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    public PublicCatalogController(ProductRepository productRepo, CategoryRepository categoryRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> categories() {
        List<Category> cats = categoryRepo.findAll().stream()
                .filter(Category::getIsActive).toList();
        return ResponseEntity.ok(cats.stream().map(c -> Map.<String, Object>of(
                "id", c.getId(), "name", c.getName(),
                "description", c.getDescription() != null ? c.getDescription() : ""
        )).toList());
    }

    @GetMapping("/products")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ProductResponse>> products(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            Pageable pageable) {
        Page<com.chocowholesale.backend.entity.Product> page;
        if (search != null && !search.isBlank()) {
            page = productRepo.searchByName(search.trim(), pageable);
        } else if (categoryId != null) {
            page = productRepo.findByCategoryId(categoryId, pageable);
        } else {
            page = productRepo.findAll(pageable);
        }
        return ResponseEntity.ok(page.map(ProductResponse::from));
    }

    @GetMapping("/products/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ProductResponse> productDetail(@PathVariable UUID id) {
        var product = productRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return ResponseEntity.ok(ProductResponse.from(product));
    }
}
