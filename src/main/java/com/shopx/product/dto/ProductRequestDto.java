
package com.shopx.product.dto;

import lombok.*;

@Builder
@Getter
@Setter
public class ProductRequestDto {
    private String name;
    private String description;
    private String category;
}
