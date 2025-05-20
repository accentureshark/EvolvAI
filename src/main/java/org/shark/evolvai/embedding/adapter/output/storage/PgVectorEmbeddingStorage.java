package org.shark.evolvai.embedding.adapter.output.storage;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PgVectorEmbeddingStorage implements EmbeddingStorage {

    // Quita 'final' para permitir el mock en tests
    private final PgVectorEmbeddingStore embeddingStore;

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
    }

    @Override
    public void store(String id, Embedding embedding, String text) {
        // Pasa el texto como TextSegment
        embeddingStore.add(id, embedding);
    }

    @Override
    public List<EmbeddingMatch<String>> findSimilar(Embedding embedding, int maxResults, double minScore) {
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(embedding, maxResults);

        return matches.stream()
                .filter(match -> match.score() >= minScore)
                .map(match -> new EmbeddingMatch<>(
                        match.score(),
                        match.embeddingId(),
                        match.embedding(),
                        match.embedded() != null ? match.embedded().text() : ""
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void removeAll() {
        throw new UnsupportedOperationException("No implementado para PgVector");
    }
}