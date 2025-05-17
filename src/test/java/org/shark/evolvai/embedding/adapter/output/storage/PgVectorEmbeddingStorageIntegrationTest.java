
package org.shark.evolvai.embedding.adapter.output.storage;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PgVectorEmbeddingStorageIntegrationTest {

    private PgVectorEmbeddingStorage storage;

    @BeforeEach
    void setUp() {
        // Limpia la tabla antes de cada test
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/vectors", "postgres", "postgres1234")) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM embeddings");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        storage = new PgVectorEmbeddingStorage(
                "localhost", 5432, "vectors", "postgres", "postgres1234", "embeddings", 2
        );
    }

    @Test
    void testStoreAndFindSimilar() {
        Embedding emb1 = Embedding.from(new float[]{1.0f, 2.0f});
        Embedding emb2 = Embedding.from(new float[]{1.1f, 2.1f});
        String id1 = "b3b8c7e2-8c2a-4e2a-9b2a-1b2a3c4d5e6f";
        String id2 = "c4d5e6f7-1a2b-3c4d-5e6f-7a8b9c0d1e2f";
        storage.store(id1, emb1, "texto1");
        storage.store(id2, emb2, "texto2");

        List<EmbeddingMatch<String>> matches = storage.findSimilar(emb1, 2, 0.0);

        assertFalse(matches.isEmpty());
        assertTrue(matches.stream().anyMatch(m -> id1.equals(m.embeddingId())));
        assertTrue(matches.stream().anyMatch(m -> id2.equals(m.embeddingId())));
    }

    @Test
    void testRemoveAllThrows() {
        assertThrows(UnsupportedOperationException.class, () -> storage.removeAll());
    }
}