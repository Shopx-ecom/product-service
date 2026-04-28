
package com.shopx.product.mapper;

import com.shopx.product.entity.Inventory;
import com.shopx.product.dto.*;

import java.util.HashMap;
import java.util.Map;

public class InventoryMapper {

    public static Inventory toEntity(InventoryRequestDto dto) {
        return Inventory.builder()
                .productId(dto.getProductId())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .build();
    }

    public static InventoryResponseDto toResponse(Inventory i) {
        InventoryResponseDto dto = new InventoryResponseDto();
        dto.setId(i.getId());
        dto.setProductId(i.getProductId());
        dto.setSellerId(i.getSellerId());
        dto.setQuantity(i.getQuantity());
        dto.setPrice(i.getPrice());
        return dto;
    }

    public static Map<String, Object> toUpdateMap(InventoryUpdateDto dto) {
        Map<String, Object> map = new HashMap<>();

        if (dto.getQuantity() != null)
            map.put("quantity", dto.getQuantity());

        if (dto.getPrice() != null)
            map.put("price", dto.getPrice());

        return map;
    }
}
