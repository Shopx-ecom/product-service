package com.shopx.product.controller;

import com.shopx.product.core.Constants;
import com.shopx.product.core.DefaultFilter;
import com.shopx.product.core.FindResourceOption;
import com.shopx.product.core.PageResponse;
import com.shopx.product.dto.*;
import com.shopx.product.entity.Product;
import com.shopx.product.filter.ProductFilter;
import com.shopx.product.mapper.ProductMapper;
import com.shopx.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Endpoint to create product")
    @PostMapping
    public ResponseEntity<ProductResponseDto> create(
            @Valid @RequestBody ProductRequestDto dto,
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute(Constants.SESSION_USER_ID);

        Product saved = service.createProduct(
                ProductMapper.toEntity(dto)
        );

        saved.setCreatedBy(userId);
        return ResponseEntity.ok(ProductMapper.toDto(saved));
    }

    @PreAuthorize("hasAnyRole('SELLER','ADMIN','CUSTOMER')")
    @Operation(summary = "Endpoint to get product by id")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getById(@PathVariable Long id) {
        Product product = service.getProductById(id);
        return ResponseEntity.ok(ProductMapper.toDto(product));
    }

    @PreAuthorize("hasAnyRole('SELLER','ADMIN','CUSTOMER')")
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

    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Endpoint to update product")
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateDto dto,
            HttpServletRequest request
    ) {

        Long userId = (Long) request.getAttribute(Constants.SESSION_USER_ID);
        Product updated = service.updateProduct(
                id,
                ProductMapper.toUpdateMap(dto)
        );
        return ResponseEntity.ok(ProductMapper.toDto(updated));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Endpoint to delete product")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test")
    public String test(
            HttpServletRequest request
    ){
        return "service running.\nActor id : "+request.getAttribute(Constants.SESSION_ACTOR_ID)+"\nUser if : "+request.getAttribute(Constants.SESSION_USER_ID);
    }
}