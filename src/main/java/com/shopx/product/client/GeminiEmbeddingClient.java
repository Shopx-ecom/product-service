package com.shopx.product.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Calls Gemini text-embedding-004 to get 768-dimensional float embeddings.
 * Free tier: 1500 requests/day.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiEmbeddingClient {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.embedding.url}")
    private String embeddingUrl;

    /**
     * Returns a float[] of length 768 for the given text.
     */
    @SuppressWarnings("unchecked")
    public float[] embed(String text) {
        String url = embeddingUrl + "?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "model", "models/text-embedding-004",
                "content", Map.of(
                        "parts", List.of(Map.of("text", text))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        if (response.getBody() == null)
            throw new RuntimeException("Empty response from Gemini Embedding API");

        Map<String, Object> embeddingObj = (Map<String, Object>) response.getBody().get("embedding");
        List<Double> values = (List<Double>) embeddingObj.get("values");

        float[] result = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i).floatValue();
        }
        return result;
    }

    /**
     * Converts float[] to pgvector string format: [0.1,0.2,...,0.n]
     */
    public static String toVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
