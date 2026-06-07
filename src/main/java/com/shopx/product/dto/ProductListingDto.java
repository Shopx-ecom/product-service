package com.shopx.product.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListingDto {

    private Long productId;
    private String name;
    private String image;
    private String category;
    private Boolean active;

    private Double lowestPrice;
    private Integer totalStock;
}