
package org.shark.evolvai.embedding.adapter.output.storage;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PgVectorEmbeddingStorageTest {

    private PgVectorEmbeddingStorage storage;
    private PgVectorEmbeddingStore mockStore;

    @BeforeEach
    void setUp() throws Exception {
        mockStore = Mockito.mock(PgVectorEmbeddingStore.class);
        storage = new PgVectorEmbeddingStorage("localhost", 5432, "postgres", "postgres", "postgres1234", "embeddings", 1536);
        Field field = PgVectorEmbeddingStorage.class.getDeclaredField("embeddingStore");
        field.setAccessible(true);
        field.set(storage, mockStore);
    }

    @Test
    void testStoreDelegatesToEmbeddingStore() {
        Embedding embedding1 = Embedding.from(new float[]{1.0f, 2.0f});
        String id1 = "b3b8c7e2-8c2a-4e2a-9b2a-1b2a3c4d5e6f";
        storage.store(id1, embedding1, "texto1");
        verify(mockStore, times(1)).add(eq(id1), eq(embedding1));
    }

    @Test
    void testFindSimilarDelegatesAndMaps() {
        Embedding embedding = Embedding.from(new float[]{1.0f, 2.0f});
        TextSegment segment = TextSegment.from("texto");
        EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(0.9, "id1", embedding, segment);
        when(mockStore.findRelevant(any(), anyInt())).thenReturn(List.of(match));

        List<EmbeddingMatch<String>> result = storage.findSimilar(embedding, 1, 0.5);

        assertEquals(1, result.size());
        assertEquals("id1", result.get(0).embeddingId());
        assertEquals("texto", result.get(0).embedded());
        assertEquals(0.9, result.get(0).score());
    }

    @Test
    void testRemoveAllThrows() {
        assertThrows(UnsupportedOperationException.class, () -> storage.removeAll());
    }
}