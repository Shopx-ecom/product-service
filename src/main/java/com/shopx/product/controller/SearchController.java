package com.shopx.product.controller;

import com.shopx.product.dto.ProductListingDto;
import com.shopx.product.service.EmbeddingIndexService;
import com.shopx.product.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final EmbeddingIndexService embeddingIndexService;

    /**
     * Full semantic search.
     * GET /api/v1/search?query=comfortable running shoes
     */
    @GetMapping
    public ResponseEntity<List<ProductListingDto>> search(@RequestParam String query) {
        return ResponseEntity.ok(searchService.search(query));
    }

    /**
     * Lightweight suggestions while user is typing.
     * Returns top 5 results with name + category + lowestPrice.
     * Minimum 2 characters required.
     * GET /api/v1/search/suggestions?query=run
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<ProductListingDto>> suggestions(@RequestParam String query) {
        if (query == null || query.trim().length() < 2) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(searchService.suggest(query));
    }

    @PostMapping("/reindex")
    public ResponseEntity<String> reindex() {
        embeddingIndexService.reindexAll();
        return ResponseEntity.ok("Reindex triggered");
    }

    @PostMapping("/index/{productId}")
    public ResponseEntity<String> indexOne(@PathVariable Long productId) {
        embeddingIndexService.indexProduct(productId);
        return ResponseEntity.ok("Indexing triggered for product " + productId);
    }
}
