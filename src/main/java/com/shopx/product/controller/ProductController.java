package com.shopx.product.controller;

import com.shopx.product.core.DefaultFilter;
import com.shopx.product.core.FindResourceOption;
import com.shopx.product.core.PageResponse;
import com.shopx.product.dto.*;
import com.shopx.product.entity.Product;
import com.shopx.product.filter.ProductFilter;
import com.shopx.product.mapper.ProductMapper;
import com.shopx.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @Operation(summary = "Endpoint to create product")
    @PostMapping
    public ResponseEntity<ProductResponseDto> create(
            @Valid @RequestBody ProductRequestDto dto
    ) {
        Product saved = service.createProduct(
                ProductMapper.toEntity(dto)
        );
        return ResponseEntity.ok(ProductMapper.toDto(saved));
    }

    @Operation(summary = "Endpoint to get product by id")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getById(@PathVariable Long id) {
        Product product = service.getProductById(id);
        return ResponseEntity.ok(ProductMapper.toDto(product));
    }

    @Operation(summary = "Endpoint to fetch products with filter")
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponseDto>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {

        PageResponse<Product> response = service.getAllProducts(
                ProductFilter.builder()
                        .name(name)
                        .category(category)
                        .active(active)
                        .search(search)
                        .build(),
                FindResourceOption.builder()
                        .offset(page)
                        .limit(size)
                        .sortOrder(sortOrder)
                        .build(),
                DefaultFilter.builder().build()
        );

        return ResponseEntity.ok(
                response.map(ProductMapper::toDto)
        );
    }

    @Operation(summary = "Endpoint to update product")
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateDto dto
    ) {
        Product updated = service.updateProduct(
                id,
                ProductMapper.toUpdateMap(dto)
        );
        return ResponseEntity.ok(ProductMapper.toDto(updated));
    }

    @Operation(summary = "Endpoint to delete product")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}