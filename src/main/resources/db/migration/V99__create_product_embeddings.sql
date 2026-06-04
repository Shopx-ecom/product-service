CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS product_embeddings (
    product_id BIGINT PRIMARY KEY,
    embedding  vector(3072) NOT NULL,
    content    TEXT NOT NULL
);
