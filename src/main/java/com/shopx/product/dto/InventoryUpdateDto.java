package com.shopx.product.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUpdateDto {

    private Integer quantity;
    private Double price;
}