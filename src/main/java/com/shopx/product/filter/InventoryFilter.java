package com.shopx.product.filter;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
public class InventoryFilter {

    private Long id;
    private List<Long> ids;

    private Long productId;
    private List<Long> productIds;

    private Long sellerId;
    private List<Long> sellerIds;

    private Integer minQuantity;
    private Integer maxQuantity;

    private Double minPrice;
    private Double maxPrice;
}