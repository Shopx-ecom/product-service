package com.shopx.product.service;

import com.shopx.product.client.GeminiEmbeddingClient;
import com.shopx.product.core.DefaultFilter;
import com.shopx.product.core.FindResourceOption;
import com.shopx.product.dto.ProductListingDto;
import com.shopx.product.entity.Inventory;
import com.shopx.product.entity.Product;
import com.shopx.product.filter.InventoryFilter;
import com.shopx.product.filter.ProductFilter;
import com.shopx.product.repository.ProductEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final GeminiEmbeddingClient geminiEmbeddingClient;
    private final ProductEmbeddingRepository embeddingRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;

    @Value("${search.top-k:10}")
    private int topK;

    private static final int SUGGESTION_TOP_K = 5;

    /**
     * Full semantic search — returns top-K results.
     */
    public List<ProductListingDto> search(String userQuery) {
        return executeSearch(userQuery, topK);
    }

    /**
     * Suggestion search — same pipeline but only top 5, called while user is typing.
     */
    public List<ProductListingDto> suggest(String userQuery) {
        return executeSearch(userQuery, SUGGESTION_TOP_K);
    }

    /**
     * Core RAG pipeline shared by both search and suggest.
     *
     * 1. Embed the user query with Gemini text-embedding-004
     * 2. Cosine similarity search against pgvector
     * 3. Fetch Product + Inventory for matched IDs
     * 4. Return ProductListingDto in similarity order
     */
    private List<ProductListingDto> executeSearch(String userQuery, int k) {

        float[] queryEmbedding = geminiEmbeddingClient.embed(userQuery);
        String queryVector = GeminiEmbeddingClient.toVectorString(queryEmbedding);

        List<Long> topProductIds = embeddingRepository.findTopKSimilar(queryVector, k);
        if (topProductIds.isEmpty()) return List.of();

        List<Product> products = productService.getAllProducts(
                ProductFilter.builder().ids(topProductIds).active(true).build(),
                FindResourceOption.builder().build(),
                DefaultFilter.builder().build()
        ).getData();

        if (products.isEmpty()) return List.of();

        List<Inventory> inventoryList = inventoryService.getAllInventory(
                InventoryFilter.builder().productIds(topProductIds).build(),
                FindResourceOption.builder().build(),
                DefaultFilter.builder().build()
        ).getData();

        Map<Long, List<Inventory>> groupedInventory = inventoryList.stream()
                .collect(Collectors.groupingBy(Inventory::getProductId));

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return topProductIds.stream()
                .filter(productMap::containsKey)
                .map(id -> {
                    Product p = productMap.get(id);
                    List<Inventory> inv = groupedInventory.getOrDefault(id, List.of());

                    Double lowestPrice = inv.stream()
                            .map(Inventory::getPrice)
                            .min(Double::compareTo)
                            .orElse(null);

                    Integer totalStock = inv.stream()
                            .mapToInt(Inventory::getQuantity)
                            .sum();

                    return ProductListingDto.builder()
                            .productId(p.getId())
                            .name(p.getName())
                            .category(p.getCategory())
                            .active(p.getActive())
                            .lowestPrice(lowestPrice)
                            .totalStock(totalStock)
                            .build();
                })
                .toList();
    }
}
