package com.shopx.product.kafka;

import com.shopx.common.dto.OrderItemResponseDto;
import com.shopx.common.event.OrderEvent;
import com.shopx.product.exception.NotFoundException;
import com.shopx.product.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Sameer Shaikh
 * @date 14-05-2026
 * @description
 */

@RequiredArgsConstructor
@Slf4j
@Component
public class Consumer {

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = "order-events",
            groupId = "inventory-group"
    )
    public void consumeOrderEvent(OrderEvent orderEvent){

        if(orderEvent.getEventType()!=null && orderEvent.getEventType().equals("order-confirmed")){
            log.info("Order confirmed event consumed");

            List<OrderItemResponseDto> orderItemResponseDtoList = orderEvent.getItems();
            if(orderItemResponseDtoList.isEmpty())
                throw new NotFoundException("order items not found.");

            inventoryService.bulkUpdateInventoryQuantity(orderItemResponseDtoList);

        }

    }

}
