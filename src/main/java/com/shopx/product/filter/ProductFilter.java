package com.shopx.product.filter;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
public class ProductFilter {

    private Long id;
    private List<Long> ids;

    private String name;
    private String category;
    private Boolean active;

    // search support
    private String search;
}