package org.shark.evolvai.inference.adapter.in.rest;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shark.evolvai.chathistory.port.in.ChatMemoryService;
import org.shark.evolvai.config.RagProperties;
import org.shark.evolvai.embedding.port.out.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.out.EmbeddingStorage;
import org.shark.evolvai.inference.controller.QueryResponse;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.service.InferenceService;
import org.shark.evolvai.llm.port.out.LlmProvider;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InferenceServiceTest {

    private InferenceService inferenceService;
    private EmbeddingGenerator embeddingGenerator;
    private EmbeddingStorage embeddingStorage;
    private LlmProvider llmProvider;
    private ChatMemoryService chatMemoryService;

    @BeforeEach
    void setUp() {
        embeddingGenerator = mock(EmbeddingGenerator.class);
        embeddingStorage = mock(EmbeddingStorage.class);
        llmProvider = mock(LlmProvider.class);
        chatMemoryService = mock(ChatMemoryService.class);

        // Configura RagProperties con la subclase Inference
        RagProperties ragProperties = new RagProperties();
        RagProperties.Inference inference = new RagProperties.Inference();
        inference.setMaxResults(3);
        inference.setMinScore(0.5);
        ragProperties.setInference(inference);

        inferenceService = new InferenceService(
                embeddingGenerator,
                embeddingStorage,
                llmProvider,
                chatMemoryService,
                ragProperties
        );
    }

    @Test
    void testAdvancedQuery_withMetadataFilter() {
        RagQueryRequest request = new RagQueryRequest();
        request.setQuery("¿Qué tareas realiza un Tech Lead en Level 12?");
        request.setDocumentId("plan-carrera");
        request.setRol("Tech Lead");
        request.setNivel("Level 12");
        request.setContextMetadata(Map.of("organizacion", "Accenture"));
        request.setCustomPrompt("Respondé con base en contexto");
        request.setMaxResults(2);
        request.setMinSimilarity(0.7);
        request.setIncludeMatches(true);

        Embedding dummyEmbedding = new Embedding(new float[]{1.0f, 2.0f});
        when(embeddingGenerator.generateEmbedding(request.getQuery())).thenReturn(dummyEmbedding);

        EmbeddingMatch<String> match = new EmbeddingMatch<>(
                0.88,
                "doc1",
                dummyEmbedding,
                "Participar en la charla de comunidad..."
        );

        when(embeddingStorage.findSimilar(
                eq(dummyEmbedding),
                eq(2),
                eq(0.7),
                any(Metadata.class)
        )).thenReturn(List.of(match));

        when(llmProvider.generateResponse(any(), eq(request.getQuery()), eq(request.getCustomPrompt())))
                .thenReturn("Un Tech Lead en Level 12 debe participar en iniciativas de comunidad.");

        when(chatMemoryService.getMessages(any())).thenReturn(List.of());

        QueryResponse response = inferenceService.advancedQuery(request);

        assertNotNull(response);
        assertEquals("Un Tech Lead en Level 12 debe participar en iniciativas de comunidad.", response.getAnswer());
        assertEquals(1, response.getMatches().size());
        assertNotNull(response.getConversationId());
    }
}