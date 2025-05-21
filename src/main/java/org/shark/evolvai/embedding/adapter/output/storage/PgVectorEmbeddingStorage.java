package org.shark.evolvai.embedding.adapter.output.storage;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PgVectorEmbeddingStorage implements EmbeddingStorage {

    private static final Logger log = LoggerFactory.getLogger(PgVectorEmbeddingStorage.class);

    private final PgVectorEmbeddingStore embeddingStore;

    public PgVectorEmbeddingStorage(
            @Value("${embedding.pgvector.host}") String host,
            @Value("${embedding.pgvector.port}") int port,
            @Value("${embedding.pgvector.database}") String database,
            @Value("${embedding.pgvector.user}") String user,
            @Value("${embedding.pgvector.password}") String password,
            @Value("${embedding.pgvector.tableName}") String tableName,
            @Value("${embedding.pgvector.dimensions}") int dimensions
    ) {
        try {
            this.embeddingStore = PgVectorEmbeddingStore.builder()
                    .host(host)
                    .port(port)
                    .database(database)
                    .user(user)
                    .password(password)
                    .table(tableName)
                    .dimension(dimensions)  // <- IMPORTANTE: agrega esto
                    .build();
        } catch (Exception e) {
            log.error("Error inicializando PgVectorEmbeddingStore", e);
            throw e;
        }
    }

    @Override
    public void store(String id, Embedding embedding, String text) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("documentName", id);
        meta.put("usuario", "desconocido");
        meta.put("timestamp", Instant.now().toEpochMilli());

        Metadata metadata = Metadata.from(meta);
        TextSegment segment = TextSegment.from(text, metadata);

        // Ajustar a 1536 dimensiones
        float[] original = embedding.vector();
        if (original.length > 1536) {
            throw new IllegalArgumentException("Embedding tiene más de 1536 dimensiones: " + original.length);
        }
        float[] padded = new float[1536];
        System.arraycopy(original, 0, padded, 0, original.length);

        Embedding paddedEmbedding = new Embedding(padded);
        embeddingStore.add(paddedEmbedding, segment);
        log.info("Embedding almacenado en PgVector con id={}, metadatos={}", id, metadata);
    }

    @Override
    public List<EmbeddingMatch<String>> findSimilar(Embedding embedding, int maxResults, double minScore) {
        float[] original = embedding.vector();
        if (original.length > 1536) {
            throw new IllegalArgumentException("Embedding de búsqueda excede las 1536 dimensiones: " + original.length);
        }
        float[] padded = new float[1536];
        System.arraycopy(original, 0, padded, 0, original.length);

        Embedding paddedEmbedding = new Embedding(padded);

        return embeddingStore.findRelevant(paddedEmbedding, maxResults, minScore)
                .stream()
                .map(match -> new EmbeddingMatch<>(
                        match.score(),
                        match.embedded().metadata().get("documentName").toString(),  // ✅ sacamos el ID del metadata
                        match.embedding(),
                        match.embedded().text()
                ))
                .toList();

    }
    @Override
    public void removeAll() {
        embeddingStore.removeAll();
        log.warn("Todos los embeddings han sido eliminados de PgVector.");
    }
}
