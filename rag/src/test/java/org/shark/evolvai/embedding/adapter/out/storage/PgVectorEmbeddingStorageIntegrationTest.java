package org.shark.evolvai.embedding.adapter.out.storage;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PgVectorEmbeddingStorageIntegrationTest {

    private PgVectorEmbeddingStorage storage;
    private static final String HOST = "localhost";
    private static final int PORT = 5432;
    private static final String DB = "evolvai";
    private static final String USER = "postgres";
    private static final String PASS = "postgres1234";
    private static final String TABLE = "embeddings";
    private static final int DIM = 1536;

    @BeforeEach
    void setUp() {
        storage = new PgVectorEmbeddingStorage(HOST, PORT, DB, USER, PASS, TABLE, DIM);
        clearTable();
    }

    @AfterEach
    void tearDown() {
        clearTable();
    }

    private void clearTable() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB, USER, PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM " + TABLE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testStoreAndFindSimilar() {
        float[] vector1 = new float[DIM];
        vector1[0] = 1.0f;
        vector1[1] = 2.0f;

        float[] vector2 = new float[DIM];
        vector2[0] = 1.1f;
        vector2[1] = 2.1f;

        Embedding emb1 = Embedding.from(vector1);
        Embedding emb2 = Embedding.from(vector2);

        String id1 = "b3b8c7e2-8c2a-4e2a-9b2a-1b2a3c4d5e6f";
        String id2 = "c4c9d8e2-9d3b-5f3b-ac3b-2c3b4d5e6f7f";

        storage.store(id1, emb1, "texto de prueba 1");
        storage.store(id2, emb2, "texto de prueba 2");

        List<EmbeddingMatch<String>> matches = storage.findSimilar(emb1, 2, 0.0);

        assertFalse(matches.isEmpty());
        assertTrue(matches.stream().anyMatch(m -> "texto de prueba 1".equals(m.embedded())));
        assertTrue(matches.stream().anyMatch(m -> "texto de prueba 2".equals(m.embedded())));
    }

    @Test
    void testRemoveAll() {
        storage.removeAll();
        // Si no lanza excepción, el test pasa
    }
}