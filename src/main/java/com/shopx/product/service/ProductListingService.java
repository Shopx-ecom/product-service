package com.shopx.product.service;

import com.shopx.product.core.DefaultFilter;
import com.shopx.product.core.FindResourceOption;
import com.shopx.product.entity.Product;
import com.shopx.product.entity.Inventory;
import com.shopx.product.dto.ProductListingDto;
import com.shopx.product.filter.ProductFilter;
import com.shopx.product.filter.InventoryFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductListingService {

    private final ProductService productService;
    private final InventoryService inventoryService;

    public List<ProductListingDto> getProductListings(
            ProductFilter productFilter
    ) {

        // 1. fetch products
        List<Product> products = productService.getAllProducts(
                productFilter,
                FindResourceOption.builder().build(),
                DefaultFilter.builder().build()
        ).getData();

        if (products.isEmpty()) return List.of();

        // 2. get productIds
        List<Long> productIds = products.stream()
                .map(Product::getId)
                .toList();

        // 3. fetch inventory for those products
        List<Inventory> inventoryList = inventoryService.getAllInventory(
                InventoryFilter.builder()
                        .productIds(productIds)
                        .build(),
                FindResourceOption.builder().build(),
                DefaultFilter.builder().build()
        ).getData();

        // 4. group inventory by productId
        Map<Long, List<Inventory>> groupedInventory =
                inventoryList.stream()
                        .collect(Collectors.groupingBy(Inventory::getProductId));

        // 5. build response
        return products.stream().map(product -> {

            List<Inventory> inventories =
                    groupedInventory.getOrDefault(product.getId(), List.of());

            Double lowestPrice = inventories.stream()
                    .map(Inventory::getPrice)
                    .min(Double::compareTo)
                    .orElse(null);

            Integer totalStock = inventories.stream()
                    .map(Inventory::getQuantity)
                    .reduce(0, Integer::sum);

            return ProductListingDto.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .category(product.getCategory())
                    .active(product.getActive())
                    .lowestPrice(lowestPrice)
                    .totalStock(totalStock)
                    .build();

        }).toList();
    }
}