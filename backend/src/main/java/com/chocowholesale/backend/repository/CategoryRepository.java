package com.chocowholesale.backend.repository;

import com.chocowholesale.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByIsActiveTrue();
    boolean existsByName(String name);
}
