
package com.shopx.product.service;

import com.shopx.common.dto.OrderItemResponseDto;
import com.shopx.product.core.*;
import com.shopx.product.dto.InventoryBulkRequest;
import com.shopx.product.dto.InventoryResponseDto;
import com.shopx.product.entity.Inventory;
import com.shopx.product.exception.NotFoundException;
import com.shopx.product.filter.InventoryFilter;
import com.shopx.product.mapper.InventoryMapper;
import com.shopx.product.repository.InventoryRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService extends ResourceService<Inventory> {

    private final InventoryRepository repository;

    protected Class<Inventory> getEntityType() { return Inventory.class; }
    protected JpaRepository<Inventory, Long> getRepository() { return repository; }
    protected JpaSpecificationExecutor<Inventory> getSpecificationExecutorRepository() { return repository; }
    protected String getResourceName() { return "inventory"; }

    @Override
    protected Specification<Inventory> getPassedFilters(Object filters, DefaultFilter defaultFilter) {

        Specification<Inventory> parentSpec = super.getPassedFilters(filters, defaultFilter);
        InventoryFilter filter = (InventoryFilter) filters;

        Specification<Inventory> childSpec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (filter.getId() != null)
                predicate = cb.and(predicate, cb.equal(root.get("id"), filter.getId()));

            if (filter.getIds() != null && !filter.getIds().isEmpty())
                predicate = cb.and(predicate, root.get("id").in(filter.getIds()));

            if (filter.getProductId() != null)
                predicate = cb.and(predicate, cb.equal(root.get("productId"), filter.getProductId()));

            if (filter.getProductIds() != null && !filter.getProductIds().isEmpty())
                predicate = cb.and(predicate, root.get("productId").in(filter.getProductIds()));

            if (filter.getSellerId() != null)
                predicate = cb.and(predicate, cb.equal(root.get("sellerId"), filter.getSellerId()));

            if (filter.getSellerIds() != null && !filter.getSellerIds().isEmpty())
                predicate = cb.and(predicate, root.get("sellerId").in(filter.getSellerIds()));

            if (filter.getMinQuantity() != null)
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("quantity"), filter.getMinQuantity()));

            if (filter.getMaxQuantity() != null)
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("quantity"), filter.getMaxQuantity()));

            if (filter.getMinPrice() != null)
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));

            if (filter.getMaxPrice() != null)
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));

            return predicate;
        };

        return Specification.where(parentSpec).and(childSpec);
    }

    public Inventory createInventory(Inventory inventory) {
        return create(inventory, Map.of());
    }

    public Inventory getInventoryById(Long id) {
        return findResource(id);
    }

    public PageResponse<Inventory> getAllInventory(
            InventoryFilter filter,
            FindResourceOption option,
            DefaultFilter defaultFilter
    ) {
        return findResources(filter, option, defaultFilter);
    }

    public Inventory updateInventory(Long id, Map<String, Object> updates) {
        return update(id, updates, Optional.empty());
    }

    public void deleteInventory(Long id) {
        Inventory inventory = findResource(id);
        inventory.setDeleted(true);
        inventory.setLastUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        getRepository().save(inventory);
    }

    public List<Inventory> getByProductId(Long productId) {
        return findResources(
                InventoryFilter.builder().productId(productId).build(),
                FindResourceOption.builder().build(),
                DefaultFilter.builder().build()
        ).getData();
    }

    public Optional<Inventory> getLowestPriceSeller(Long productId) {
        List<Inventory> list = getByProductId(productId);

        return list.stream()
                .min((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
    }

    public List<InventoryResponseDto> getByProductAndSeller(List<InventoryBulkRequest> requests) {

        List<Long> productIds = requests.stream().map(InventoryBulkRequest::getProductId).toList();
        List<Long> sellerIds = requests.stream().map(InventoryBulkRequest::getSellerId).toList();

        return findResources(
                InventoryFilter.builder()
                        .productIds(productIds)
                        .sellerIds(sellerIds)
                        .build(),
                FindResourceOption.builder().build(),
                DefaultFilter.builder().build()
        ).getData().stream().map(InventoryMapper::toResponse).toList();
    }

    @Transactional
    public void bulkUpdateInventoryQuantity(List<OrderItemResponseDto> orderItemResponseDtos) {

        Map<Long,OrderItemResponseDto> itemMap = orderItemResponseDtos.stream().collect(
                Collectors.toMap(
                        OrderItemResponseDto::getProductId,
                        Function.identity()
                )
        );

        List<Inventory> inventories = this.findResources(
                InventoryFilter.builder()
                        .productIds(orderItemResponseDtos.stream().map(OrderItemResponseDto::getProductId).toList())
                        .sellerIds(orderItemResponseDtos.stream().map(OrderItemResponseDto::getSellerId).toList())
                        .build(),
                FindResourceOption.builder().build(),
                DefaultFilter.builder().build()
        ).getData();

        if (orderItemResponseDtos.size() != inventories.size()) {
            throw new RuntimeException("Some inventories not found");
        }

        for (Inventory inventory : inventories) {

            if (inventory.getQuantity() <= 0) {
                throw new RuntimeException(
                        "Insufficient inventory for id: " + inventory.getId()
                );
            }

            OrderItemResponseDto orderItemResponseDto = itemMap.get(inventory.getProductId());
            if(orderItemResponseDto==null)
                throw new NotFoundException("order item not found");

            int requiredQuantity = orderItemResponseDto.getQuantity()==null?0:orderItemResponseDto.getQuantity();
            if(requiredQuantity<=0 || requiredQuantity>inventory.getQuantity())
                throw new NotFoundException("Invalid quantity");

            inventory.setQuantity(
                    inventory.getQuantity() - requiredQuantity
            );
        }

        repository.saveAll(inventories);

        log.info("Stock reduced");
    }

}
