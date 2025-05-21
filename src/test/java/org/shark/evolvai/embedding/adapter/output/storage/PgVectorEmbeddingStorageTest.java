package org.shark.evolvai.embedding.adapter.output.storage;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PgVectorEmbeddingStorageTest {

    private PgVectorEmbeddingStorage storage;
    private PgVectorEmbeddingStore mockStore;

    @BeforeEach
    void setUp() throws Exception {
        mockStore = mock(PgVectorEmbeddingStore.class);
        new PgVectorEmbeddingStorage("localhost", 5432, "evolvai", "postgres", "postgres1234", "embeddings", 1536);
        Field field = PgVectorEmbeddingStorage.class.getDeclaredField("embeddingStore");
        field.setAccessible(true);
        field.set(storage, mockStore);
    }

    @Test
    void testStoreDelegatesToEmbeddingStore() {
        Embedding embedding = Embedding.from(new float[]{1.0f, 2.0f});
        storage.store("id1", embedding, "text1");
        verify(mockStore).add(eq(embedding), any(TextSegment.class));
    }

    @Test
    void testFindSimilarDelegatesAndMaps() {
        Embedding embedding = Embedding.from(new float[]{1.0f, 2.0f});
        TextSegment segment = TextSegment.from("text");
        // El constructor requiere: Double score, String id, Embedding embedding, TextSegment embedded
        when(mockStore.findRelevant(any(), anyInt(), anyDouble()))
                .thenReturn(List.of(new EmbeddingMatch<TextSegment>(0.9, null, embedding, segment)));

        List<EmbeddingMatch<String>> result = storage.findSimilar(embedding, 1, 0.5);

        assertEquals(1, result.size());
        assertEquals("text", result.get(0).embedded());
        assertEquals(0.9, result.get(0).score());
    }

    @Test
    void testRemoveAll() {
        storage.removeAll();
        verify(mockStore).removeAll();
    }
}