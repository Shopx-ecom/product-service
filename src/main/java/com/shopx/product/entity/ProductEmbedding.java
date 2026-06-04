package com.shopx.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_embeddings")
public class ProductEmbedding {

    @Id
    @Column(name = "product_id")
    private Long productId;

    /**
     * Stored as vector(768) in PostgreSQL via pgvector.
     * Gemini text-embedding-004 produces 768-dim vectors.
     * Mapped as float[] here; the custom type handles DB serialization.
     */
    @Column(name = "embedding", columnDefinition = "vector(3072)", nullable = false)
    private float[] embedding;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content; // the text that was embedded (for debugging / re-indexing)
}
