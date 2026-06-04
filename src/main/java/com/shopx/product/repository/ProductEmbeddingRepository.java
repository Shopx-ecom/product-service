package com.shopx.product.repository;

import com.shopx.product.entity.ProductEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductEmbeddingRepository extends JpaRepository<ProductEmbedding, Long> {

    /**
     * Cosine similarity search using pgvector <=> operator.
     * Returns top-k most similar product IDs ordered by distance (lower = more similar).
     * Cast ::vector is required because Spring passes the array as text.
     */
    @Query(value = """
            SELECT product_id
            FROM product_embeddings
            ORDER BY embedding <=> CAST(:queryVector AS vector)
            LIMIT :topK
            """, nativeQuery = true)
    List<Long> findTopKSimilar(@Param("queryVector") String queryVector,
                               @Param("topK") int topK);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO product_embeddings (product_id, embedding, content)
            VALUES (:productId, CAST(:embedding AS vector), :content)
            ON CONFLICT (product_id) DO UPDATE
              SET embedding = CAST(:embedding AS vector),
                  content   = :content
            """, nativeQuery = true)
    void upsertEmbedding(@Param("productId") Long productId,
                         @Param("embedding") String embedding,
                         @Param("content") String content);
}
