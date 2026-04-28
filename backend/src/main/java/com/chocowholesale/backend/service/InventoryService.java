package com.chocowholesale.backend.service;

import com.chocowholesale.backend.dto.InventoryResponse;
import com.chocowholesale.backend.dto.StockAdjustRequest;
import com.chocowholesale.backend.entity.Inventory;
import com.chocowholesale.backend.entity.StockMovement;
import com.chocowholesale.backend.entity.User;
import com.chocowholesale.backend.repository.InventoryRepository;
import com.chocowholesale.backend.repository.StockMovementRepository;
import com.chocowholesale.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            StockMovementRepository stockMovementRepository,
                            UserRepository userRepository) {
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> listAll() {
        return inventoryRepository.findAll().stream()
                .map(InventoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> listLowStock() {
        return inventoryRepository.findLowStock().stream()
                .map(InventoryResponse::from).toList();
    }

    @Transactional
    public InventoryResponse adjustStock(UUID productId, StockAdjustRequest req, UUID actorUserId) {
        Inventory inv = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for product"));

        inv.setTotalStock(inv.getTotalStock() + req.quantity());
        if (inv.getTotalStock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock cannot go below zero");
        }
        inventoryRepository.save(inv);

        User actor = userRepository.findById(actorUserId).orElse(null);
        StockMovement movement = new StockMovement();
        movement.setProduct(inv.getProduct());
        movement.setQuantity(req.quantity());
        movement.setReason(req.reason());
        movement.setNote(req.note());
        movement.setActor(actor);
        stockMovementRepository.save(movement);

        return InventoryResponse.from(inventoryRepository.findByProductId(productId).orElseThrow());
    }
}
