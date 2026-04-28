package com.chocowholesale.backend.web;

import com.chocowholesale.backend.dto.InventoryResponse;
import com.chocowholesale.backend.dto.StockAdjustRequest;
import com.chocowholesale.backend.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/inventory")
public class AdminInventoryController {

    private final InventoryService inventoryService;

    public AdminInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> list() {
        return ResponseEntity.ok(inventoryService.listAll());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryResponse>> lowStock() {
        return ResponseEntity.ok(inventoryService.listLowStock());
    }

    @PostMapping("/{productId}/adjust")
    public ResponseEntity<InventoryResponse> adjust(
            @PathVariable UUID productId,
            @Valid @RequestBody StockAdjustRequest request,
            @AuthenticationPrincipal UUID actorUserId) {
        return ResponseEntity.ok(inventoryService.adjustStock(productId, request, actorUserId));
    }
}
