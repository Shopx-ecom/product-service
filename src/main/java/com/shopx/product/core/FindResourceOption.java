package com.shopx.product.core;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class FindResourceOption {
    private Integer offset;
    private Integer limit;
    private String sortOrder;
    private String sortBy;
}
