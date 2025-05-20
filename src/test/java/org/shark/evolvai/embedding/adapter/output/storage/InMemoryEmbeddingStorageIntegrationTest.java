package org.shark.evolvai.embedding.adapter.output.storage;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryEmbeddingStorageIntegrationTest {

    private InMemoryEmbeddingStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryEmbeddingStorage();
    }

    @Test
    void testStoreAndFindSimilar() {
        Embedding emb1 = Embedding.from(new float[]{1.0f, 2.0f});
        Embedding emb2 = Embedding.from(new float[]{1.1f, 2.1f});
        storage.store("id1", emb1, "texto1");
        storage.store("id2", emb2, "texto2");

        List<EmbeddingMatch<String>> matches = storage.findSimilar(emb1, 2, 0.0);

        assertFalse(matches.isEmpty());
        assertTrue(matches.stream().anyMatch(m -> m.embeddingId().equals("id1")));
        assertTrue(matches.stream().anyMatch(m -> m.embeddingId().equals("id2")));
    }


}
