package com.shopx.product.service;

import com.shopx.product.client.GeminiEmbeddingClient;
import com.shopx.product.core.DefaultFilter;
import com.shopx.product.core.FindResourceOption;
import com.shopx.product.entity.Inventory;
import com.shopx.product.entity.Product;
import com.shopx.product.filter.InventoryFilter;
import com.shopx.product.filter.ProductFilter;
import com.shopx.product.repository.ProductEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Responsible for:
 * 1. Building a human-readable text chunk per product (name + description + category + price range)
 * 2. Calling Gemini to get the embedding
 * 3. Upserting into product_embeddings via pgvector
 *
 * Call indexProduct() whenever a product/inventory is created or updated.
 * Call reindexAll() on startup or via admin endpoint.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingIndexService {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final GeminiEmbeddingClient geminiEmbeddingClient;
    private final ProductEmbeddingRepository embeddingRepository;

    /**
     * Build the text chunk for a product.
     * Richer text = better semantic search quality.
     */
    public String buildChunk(Product product, List<Inventory> inventories) {
        double lowestPrice = inventories.stream()
                .mapToDouble(Inventory::getPrice).min().orElse(0);
        double highestPrice = inventories.stream()
                .mapToDouble(Inventory::getPrice).max().orElse(0);
        int totalStock = inventories.stream()
                .mapToInt(Inventory::getQuantity).sum();

        return String.format(
                "Product: %s. Category: %s. Description: %s. " +
                "Price range: %.2f to %.2f. Total stock available: %d.",
                product.getName(),
                product.getCategory() != null ? product.getCategory() : "Uncategorized",
                product.getDescription() != null ? product.getDescription() : "",
                lowestPrice, highestPrice, totalStock
        );
    }

    /**
     * Index a single product by ID.
     * Called async after create/update events.
     */
    @Async
    public void indexProduct(Long productId) {
        try {
            Product product = productService.getProductById(productId);
            List<Inventory> inventories = inventoryService.getByProductId(productId);

            String chunk = buildChunk(product, inventories);
            float[] embedding = geminiEmbeddingClient.embed(chunk);
            String vectorStr = GeminiEmbeddingClient.toVectorString(embedding);

            embeddingRepository.upsertEmbedding(productId, vectorStr, chunk);
            log.info("Indexed product {}", productId);
        } catch (Exception e) {
            log.error("Failed to index product {}: {}", productId, e.getMessage());
        }
    }

    /**
     * Re-index all active products.
     * Call on startup or via admin endpoint: POST /api/v1/search/reindex
     */
    public void reindexAll() {
        List<Product> products = productService.getAllProducts(
                ProductFilter.builder().active(true).build(),
                FindResourceOption.builder().build(),
                DefaultFilter.builder().build()
        ).getData();

        if (products.isEmpty()) {
            log.info("No products to index.");
            return;
        }

        List<Long> productIds = products.stream().map(Product::getId).toList();

        List<Inventory> allInventory = inventoryService.getAllInventory(
                InventoryFilter.builder().productIds(productIds).build(),
                FindResourceOption.builder().build(),
                DefaultFilter.builder().build()
        ).getData();

        Map<Long, List<Inventory>> grouped = allInventory.stream()
                .collect(Collectors.groupingBy(Inventory::getProductId));

        for (Product product : products) {
            try {
                List<Inventory> inventories = grouped.getOrDefault(product.getId(), List.of());
                String chunk = buildChunk(product, inventories);
                float[] embedding = geminiEmbeddingClient.embed(chunk);
                String vectorStr = GeminiEmbeddingClient.toVectorString(embedding);
                embeddingRepository.upsertEmbedding(product.getId(), vectorStr, chunk);
                log.info("Reindexed product {}", product.getId());
            } catch (Exception e) {
                log.error("Failed to reindex product {}: {}", product.getId(), e.getMessage());
            }
        }

        log.info("Reindex complete: {} products", products.size());
    }
}
