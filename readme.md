# ShopX Semantic Search

**Try Semantic Search:** https://shopx-ai-search.netlify.app/

### Ever wondered how Amazon and Flipkart return exactly what you want even when your words never matched the product name? I built a similar feature
#### Developer [@Sameer Shaikh](https://github.com/Sameer377)

## Semantic Search Pipeline

![Semantic Search Pipeline](https://raw.githubusercontent.com/Shopx-ecom/product-service/master/detailed-search-pip.png)

## How It Works

```
User types a query
       ↓
GeminiEmbeddingClient          → embeds query text into float[768]
       ↓
ProductEmbeddingRepository     → cosine similarity search via pgvector <=> operator
       ↓
ProductService + InventoryService  → fetch product + stock data for matched IDs
       ↓
ProductListingDto[]            → returned in similarity order
```

When a product is created or updated, its name + description + category + price range is embedded and stored in the `product_embeddings` table. When a user searches, the query goes through the exact same embedding process and the closest vectors are retrieved.

---

## Project Structure

```
com.shopx.product
│
├── controller
│   └── SearchController.java         # GET /search, GET /search/suggestions, POST /search/reindex
│
├── service
│   ├── SearchService.java            # core RAG pipeline (shared by search + suggestions)
│   ├── EmbeddingIndexService.java    # builds + maintains the vector index
│   ├── ProductService.java           # existing product CRUD
│   └── InventoryService.java         # existing inventory CRUD
│
├── client
│   └── GeminiEmbeddingClient.java    # calls Gemini text-embedding-004 API
│
├── entity
│   ├── Product.java                  # existing product entity
│   ├── Inventory.java                # existing inventory entity
│   └── ProductEmbedding.java         # product_id + vector(768) + source text
│
├── repository
│   ├── ProductRepository.java        # existing
│   ├── InventoryRepository.java      # existing
│   └── ProductEmbeddingRepository.java  # native pgvector queries
│
└── config
    └── AppConfig.java                # RestTemplate bean + @EnableAsync

resources/
├── db/migration/
│   └── V99__create_product_embeddings.sql   # enables pgvector, creates table + IVFFlat index
└── application.yml                          # gemini keys + search config

frontend/
└── search.html                      # standalone single-page UI
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/search?query=` | Full semantic search, returns top 10 |
| `GET` | `/api/v1/search/suggestions?query=` | Lightweight suggestions while typing, returns top 5, min 2 chars |
| `POST` | `/api/v1/search/reindex` | Re-index all active products into pgvector |
| `POST` | `/api/v1/search/index/{productId}` | Index a single product (call after create/update) |

---

## Database

The `product_embeddings` table is separate from your existing schema — it only holds the vector representations.

```sql
CREATE TABLE product_embeddings (
    product_id BIGINT PRIMARY KEY,
    embedding  vector(768) NOT NULL,   -- Gemini text-embedding-004 output
    content    TEXT        NOT NULL    -- the text chunk that was embedded
);

CREATE INDEX idx_product_embeddings_ivfflat
    ON product_embeddings
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);
```

The IVFFlat index makes similarity queries fast at scale. `lists = 100` is suitable for up to ~1M rows.

---

## Configuration

Add to your `application.yml`:

```yaml
gemini:
  api:
    key: ${GEMINI_API_KEY}
  embedding:
    url: https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent

search:
  top-k: 10
```

Set the environment variable:

```bash
export GEMINI_API_KEY=your_key_here
```

---

## Dependencies

Add to `pom.xml`:

```xml
<dependency>
    <groupId>com.pgvector</groupId>
    <artifactId>pgvector</artifactId>
    <version>0.1.6</version>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

PostgreSQL must have the pgvector extension installed. If using Docker:

```bash
docker run -e POSTGRES_PASSWORD=pass -p 5432:5432 pgvector/pgvector:pg16
```

---

## Setup & First Run

**1. Run the Flyway migration**
Flyway picks up `V99__create_product_embeddings.sql` automatically on startup. This enables the `vector` extension and creates the table + index.

**2. Start your Spring Boot application**

**3. Trigger initial indexing**
```bash
curl -X POST http://localhost:8080/api/v1/search/reindex
```
This fetches all active products, generates embeddings via Gemini, and stores them in pgvector. Only needed once on fresh setup.

**4. Open the frontend**
Set `BASE_URL` in `search.html` to your backend URL, then open the file in a browser. Make sure CORS is enabled on `SearchController`.

---

## Keeping the Index in Sync

Call the single-product index endpoint whenever a product or its inventory changes:

```java
// inside your ProductService.createProduct / updateProduct embeddingIndexService.indexProduct(product.getId());
```

The method is `@Async` so it won't block the main request thread.

---

## How the Text Chunk Is Built

Each product is converted to a plain-text description before embedding:

```
Product: Nike Air Max. Category: Footwear. Description: Lightweight running shoe...
Price range: 1999.00 to 2499.00. Total stock available: 42.
```

Richer text produces better semantic matches. You can extend `EmbeddingIndexService.buildChunk()` to include tags, brand, or any other fields.

---

## Gemini Free Tier Limits

`text-embedding-004` on the free tier allows **1500 requests/day**. Each search and each product index counts as one request. For large catalogs, batch your reindex calls or upgrade the API plan.

---

## Frontend

`search.html` is a standalone file with no build step or dependencies. It calls your backend directly.

- Debounced 300ms on input → calls `/suggestions`
- Enter key or selecting a suggestion → calls `/search`
- Pipeline steps animate live to show which stage is running
- Set `BASE_URL` at the top of the script to point to your backend
