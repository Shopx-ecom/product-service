
package com.shopx.product.dto;

import lombok.*;

@Getter
@Setter
public class InventoryResponseDto {
    private Long id;
    private Long productId;
    private Long sellerId;
    private Integer quantity;
    private Double price;
}
