package org.shark.evolvai.embedding.adapter.output.storage;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.UUID;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PgVectorEmbeddingStorage implements EmbeddingStorage {

    private static final Logger log = LoggerFactory.getLogger(PgVectorEmbeddingStorage.class);

    private final PgVectorEmbeddingStore embeddingStore;
    private final int dimensions;


    public PgVectorEmbeddingStorage(
            @Value("${embedding.pgvector.host:localhost}") String host,
            @Value("${embedding.pgvector.port:5432}") int port,
            @Value("${embedding.pgvector.database:postgres}") String database,
            @Value("${embedding.pgvector.user:postgres}") String user,
            @Value("${embedding.pgvector.password:}") String password,
            @Value("${embedding.pgvector.tableName:embeddings}") String tableName,
            @Value("${embedding.pgvector.dimensions:1536}") int dimensions) {

        this.embeddingStore = PgVectorEmbeddingStore.builder()
                .host(host)
                .port(port)
                .database(database)
                .user(user)
                .password(password)
                .table(tableName)
                .dimension(dimensions)
                .build();
        this.dimensions = dimensions;

        log.info("PgVectorEmbeddingStore inicializado con host={}, db={}, tabla={}, dimensiones={}", host, database, tableName, dimensions);
    }

    @Override
    public void store(String id, Embedding embedding, String text) {
        log.info("Almacenando embedding con id: {}", id);
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            log.warn("El id '{}' no es un UUID válido. Se generará uno nuevo.", id);
            uuid = UUID.randomUUID();
        }

        int expectedDims = this.dimensions;
        float[] original = embedding.vector();
        float[] finalVector;

        if (original.length < expectedDims) {
            float[] padded = new float[expectedDims];
            System.arraycopy(original, 0, padded, 0, original.length);
            finalVector = padded;
            log.info("Embedding rellenado de {} a {} dimensiones", original.length, expectedDims);
        } else if (original.length > expectedDims) {
            float[] truncated = new float[expectedDims];
            System.arraycopy(original, 0, truncated, 0, expectedDims);
            finalVector = truncated;
            log.warn("Embedding truncado de {} a {} dimensiones", original.length, expectedDims);
        } else {
            finalVector = original;
        }

        log.info("Dimensión final del embedding a guardar: {}", finalVector.length);
        embeddingStore.add(uuid.toString(), Embedding.from(finalVector));
        log.info("Embedding almacenado correctamente para id: {}", uuid);
    }

    @Override
    public List<EmbeddingMatch<String>> findSimilar(Embedding embedding, int maxResults, double minScore) {
        log.info("Buscando embeddings similares (maxResults={}, minScore={})", maxResults, minScore);

        float[] original = embedding.vector();
        float[] finalVector;
        if (original.length < dimensions) {
            float[] padded = new float[dimensions];
            System.arraycopy(original, 0, padded, 0, original.length);
            finalVector = padded;
            log.info("Embedding de consulta rellenado de {} a {} dimensiones", original.length, dimensions);
        } else if (original.length > dimensions) {
            float[] truncated = new float[dimensions];
            System.arraycopy(original, 0, truncated, 0, dimensions);
            finalVector = truncated;
            log.warn("Embedding de consulta truncado de {} a {} dimensiones", original.length, dimensions);
        } else {
            finalVector = original;
        }

        Embedding adjustedEmbedding = Embedding.from(finalVector);

        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(adjustedEmbedding, maxResults);

        List<EmbeddingMatch<String>> filtered = matches.stream()
                .filter(match -> match.score() >= minScore)
                .map(match -> new EmbeddingMatch<>(
                        match.score(),
                        match.embeddingId(),
                        match.embedding(),
                        match.embedded() != null ? match.embedded().text() : ""
                ))
                .collect(Collectors.toList());
        log.info("Se encontraron {} embeddings similares (después de filtrar por minScore)", filtered.size());
        return filtered;
    }

    @Override
    public void removeAll() {
        log.warn("Intento de eliminar todos los embeddings: operación no implementada para PgVector");
        throw new UnsupportedOperationException("No implementado para PgVector");
    }
}