package org.shark.evolvai.embedding.adapter.out.storage;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class InMemoryEmbeddingStorageTest {

    private InMemoryEmbeddingStorage storage;
    private InMemoryEmbeddingStore<TextSegment> mockStore;

    @BeforeEach
    void setUp() throws Exception {
        mockStore = Mockito.mock(InMemoryEmbeddingStore.class);
        storage = new InMemoryEmbeddingStorage();
        Field field = InMemoryEmbeddingStorage.class.getDeclaredField("embeddingStore");
        field.setAccessible(true);
        field.set(storage, mockStore);
    }

    @Test
    void testStoreDelegatesToEmbeddingStore() {
        Embedding embedding = Embedding.from(new float[]{1.0f, 2.0f});
        String id = "id1";
        String text = "texto";
        storage.store(id, embedding, text);
        verify(mockStore, times(1)).add(eq(id), eq(embedding), eq(TextSegment.from(text)));
    }

    @Test
    void testFindSimilarDelegatesAndMaps() {
        Embedding embedding = Embedding.from(new float[]{1.0f, 2.0f});
        TextSegment segment = new TextSegment("texto", new Metadata());
        EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(0.9, "id1", embedding, segment);
        when(mockStore.findRelevant(any(), anyInt())).thenReturn(List.of(match));

        List<EmbeddingMatch<String>> result = storage.findSimilar(embedding, 1, 0.5);

        assertEquals(1, result.size());
        assertEquals("id1", result.get(0).embeddingId());
        assertEquals("texto", result.get(0).embedded());
        assertEquals(0.9, result.get(0).score());
    }

    // El método removeAll no está implementado en InMemoryEmbeddingStore
    // @Test
    // void testRemoveAllDelegates() {
    //     storage.removeAll();
    //     verify(mockStore, times(1)).clear();
    // }
}