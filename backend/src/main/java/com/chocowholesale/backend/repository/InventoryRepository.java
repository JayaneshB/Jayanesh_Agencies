package com.chocowholesale.backend.repository;

import com.chocowholesale.backend.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    Optional<Inventory> findByProductId(UUID productId);

    @Query("SELECT i FROM Inventory i WHERE i.available <= i.reorderThreshold")
    List<Inventory> findLowStock();

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.available <= i.reorderThreshold")
    long countLowStock();
}
