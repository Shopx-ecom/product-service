package com.shopx.product.controller;

import com.shopx.product.dto.ProductListingDto;
import com.shopx.product.filter.ProductFilter;
import com.shopx.product.service.ProductListingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/listing")
@RequiredArgsConstructor
public class ProductListingController {

    private final ProductListingService service;

    @Operation(summary = "Endpoint to fetch product listings with price and stock")
    @GetMapping
    public List<ProductListingDto> getListings(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search
    ) {
        return service.getProductListings(
                ProductFilter.builder()
                        .name(name)
                        .category(category)
                        .active(active)
                        .search(search)
                        .build()
        );
    }
}