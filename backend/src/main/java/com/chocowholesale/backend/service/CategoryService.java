package com.chocowholesale.backend.service;

import com.chocowholesale.backend.dto.CategoryRequest;
import com.chocowholesale.backend.entity.Category;
import com.chocowholesale.backend.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> listAll() {
        return categoryRepository.findAll();
    }

    public List<Category> listActive() {
        return categoryRepository.findByIsActiveTrue();
    }

    public Category create(CategoryRequest req) {
        if (categoryRepository.existsByName(req.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
        }
        Category cat = new Category();
        cat.setName(req.name());
        cat.setDescription(req.description());
        return categoryRepository.save(cat);
    }

    public Category update(UUID id, CategoryRequest req) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        cat.setName(req.name());
        cat.setDescription(req.description());
        return categoryRepository.save(cat);
    }

    public void deactivate(UUID id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        cat.setIsActive(false);
        categoryRepository.save(cat);
    }
}
