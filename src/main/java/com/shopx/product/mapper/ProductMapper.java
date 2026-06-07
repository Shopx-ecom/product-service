
package com.shopx.product.mapper;

import com.shopx.product.entity.Product;
import com.shopx.product.dto.*;

import java.util.HashMap;
import java.util.Map;

public class ProductMapper {

    public static Product toEntity(ProductRequestDto dto) {
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .active(true)
                .build();
    }

    public static ProductResponseDto toDto(Product p) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setCategory(p.getCategory());
        dto.setImage(p.getImages());
        return dto;
    }

    public static Map<String, Object> toUpdateMap(ProductUpdateDto dto) {
        Map<String, Object> map = new HashMap<>();

        if (dto.getName() != null)
            map.put("name", dto.getName());

        if (dto.getDescription() != null)
            map.put("description", dto.getDescription());

        if (dto.getCategory() != null)
            map.put("category", dto.getCategory());

        if (dto.getActive() != null)
            map.put("active", dto.getActive());

        return map;
    }
}
