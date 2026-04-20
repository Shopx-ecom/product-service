package com.shopx.product.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateDto {

    private String name;
    private String description;
    private String category;
    private Boolean active;
}