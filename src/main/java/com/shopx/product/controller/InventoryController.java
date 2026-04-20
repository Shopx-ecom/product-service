package com.shopx.product.controller;

import com.shopx.product.core.DefaultFilter;
import com.shopx.product.core.FindResourceOption;
import com.shopx.product.core.PageResponse;
import com.shopx.product.dto.InventoryBulkRequest;
import com.shopx.product.dto.InventoryRequestDto;
import com.shopx.product.dto.InventoryResponseDto;
import com.shopx.product.dto.InventoryUpdateDto;
import com.shopx.product.entity.Inventory;
import com.shopx.product.filter.InventoryFilter;
import com.shopx.product.mapper.InventoryMapper;
import com.shopx.product.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    @Operation(summary = "Endpoint to create inventory")
    @PostMapping
    public ResponseEntity<InventoryResponseDto> create(
            @Valid @RequestBody InventoryRequestDto dto
    ) {
        Inventory saved = service.createInventory(
                InventoryMapper.toEntity(dto)
        );
        return ResponseEntity.ok(InventoryMapper.toResponse(saved));
    }

    @Operation(summary = "Endpoint to get inventory by id")
    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponseDto> getById(@PathVariable Long id) {
        Inventory inventory = service.getInventoryById(id);
        return ResponseEntity.ok(InventoryMapper.toResponse(inventory));
    }

    @Operation(summary = "Endpoint to fetch inventory with filter")
    @GetMapping
    public ResponseEntity<PageResponse<InventoryResponseDto>> getAll(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) Integer minQuantity,
            @RequestParam(required = false) Integer maxQuantity,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {

        PageResponse<Inventory> response = service.getAllInventory(
                InventoryFilter.builder()
                        .productId(productId)
                        .sellerId(sellerId)
                        .minQuantity(minQuantity)
                        .maxQuantity(maxQuantity)
                        .minPrice(minPrice)
                        .maxPrice(maxPrice)
                        .build(),
                FindResourceOption.builder()
                        .offset(page)
                        .limit(size)
                        .sortOrder(sortOrder)
                        .build(),
                DefaultFilter.builder().build()
        );

        return ResponseEntity.ok(
                response.map(InventoryMapper::toResponse)
        );
    }

    @Operation(summary = "Endpoint to update inventory")
    @PatchMapping("/{id}")
    public ResponseEntity<InventoryResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody InventoryUpdateDto dto
    ) {
        Inventory updated = service.updateInventory(
                id,
                InventoryMapper.toUpdateMap(dto)
        );
        return ResponseEntity.ok(InventoryMapper.toResponse(updated));
    }

    @Operation(summary = "Endpoint to delete inventory")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

   /* @PostMapping("/get/bulk")
    public ResponseEntity< List<InventoryResponseDto>> getInventoryBulk(
            @RequestBody List<InventoryBulkRequest> requests
    ) {
        return ResponseEntity.ok(service.getByProductAndSeller(requests));
    }*/
}