package com.shopx.product.dto;

import lombok.Data;

@Data
public class InventoryBulkRequest {
    private Long productId;
    private Long sellerId;
}