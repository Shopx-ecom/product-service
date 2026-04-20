package com.shopx.product.core;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DefaultFilter {
    private Long createdBy;
    private Long updatedBy;
    private Long deletedBy;
}
