package org.shark.evolvai.embedding.provider;

import org.shark.evolvai.embedding.exception.ModelNotFoundException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import org.shark.evolvai.embedding.port.out.EmbeddingGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("ollamaEmbeddingGenerator")
public class OllamaEmbeddingGenerator implements EmbeddingGenerator {

    private static final Logger log = LoggerFactory.getLogger(OllamaEmbeddingGenerator.class);
    private final String baseUrl;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OllamaEmbeddingGenerator(
            @Value("${llm.ollama.base-url:http://ollama:11434}") String baseUrl,
            @Value("${llm.ollama.model:nomic-embed-text}") String model
    ) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.httpClient = HttpClient.newHttpClient();
        log.info("OllamaEmbeddingGenerator inicializado con baseUrl={} y model={}", baseUrl, model);
    }

    @Override
    public Embedding generateEmbedding(String text) {
        try {
            String url = baseUrl + "/api/embeddings";
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "prompt", text
            );
            String json = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                String body = response.body();
                if (body != null && body.contains("not found")) {
                    throw new ModelNotFoundException("Modelo de Ollama no encontrado: " + body);
                }
                log.error("Error al generar embedding con : {} - {}", response.statusCode(), body);
            }

            OllamaEmbeddingResponse embeddingResponse = objectMapper.readValue(response.body(), OllamaEmbeddingResponse.class);
            // Convertir List<Float> a float[]
            float[] vector = new float[embeddingResponse.embedding.size()];
            for (int i = 0; i < embeddingResponse.embedding.size(); i++) {
                vector[i] = embeddingResponse.embedding.get(i);
            }
            return new Embedding(vector);
        } catch (Exception e) {
            log.error("Error generando embedding con Ollama", e);
            throw new RuntimeException("Error generando embedding con Ollama", e);
        }
    }

    @Override
    public Embedding generateEmbedding(TextSegment segment) {
        return generateEmbedding(segment.text());
    }

    @Override
    public List<Embedding> generateEmbeddings(List<TextSegment> segments) {
        return segments.stream()
                .map(this::generateEmbedding)
                .collect(Collectors.toList());
    }

    @Override
    public Embedding generateEmbedding(int[] inputIds, int[] attentionMask) {
        throw new UnsupportedOperationException("No soportado por OllamaEmbeddingGenerator");
    }

    private static class OllamaEmbeddingResponse {
        @JsonProperty("embedding")
        public List<Float> embedding;
    }
}