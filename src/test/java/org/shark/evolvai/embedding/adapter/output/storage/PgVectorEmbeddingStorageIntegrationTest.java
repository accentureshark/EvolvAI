
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
                "localhost", 5432, "vectors", "postgres", "postgres1234", "embeddings", 1536
        );
    }

    @Test
    void testStoreAndFindSimilar() {
        // Crea vectores de 1536 dimensiones
        float[] vector1 = new float[1536];
        vector1[0] = 1.0f;
        vector1[1] = 2.0f;

        float[] vector2 = new float[1536];
        vector2[0] = 1.1f;
        vector2[1] = 2.1f;

        Embedding emb1 = Embedding.from(vector1);
        Embedding emb2 = Embedding.from(vector2);

        // UUID válidos (hexadecimales)
        String id1 = "b3b8c7e2-8c2a-4e2a-9b2a-1b2a3c4d5e6f";
        String id2 = "c4c9d8e2-9d3b-5f3b-ac3b-2c3b4d5e6f7f"; // Corregido: 'g' → 'f'

        // O mejor usa:
        // String id1 = java.util.UUID.randomUUID().toString();
        // String id2 = java.util.UUID.randomUUID().toString();

        storage.store(id1, emb1, "texto de prueba 1");
        storage.store(id2, emb2, "texto de prueba 2");

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