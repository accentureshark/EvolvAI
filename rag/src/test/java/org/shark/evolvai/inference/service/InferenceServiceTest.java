package org.shark.evolvai.inference.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.shark.evolvai.chathistory.port.in.ChatMemoryService;
import org.shark.evolvai.config.RagProperties;
import org.shark.evolvai.embedding.port.out.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.out.EmbeddingStorage;
import org.shark.evolvai.inference.controller.QueryResponse;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.llm.port.out.LlmProvider;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InferenceServiceTest {

    @Mock
    private EmbeddingGenerator embeddingGenerator;
    @Mock
    private EmbeddingStorage embeddingStorage;
    @Mock
    private LlmProvider llmProvider;
    @Mock
    private ChatMemoryService chatMemoryService;

    private InferenceService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new InferenceService(
                embeddingGenerator,
                embeddingStorage,
                llmProvider,
                chatMemoryService,
                buildProps()
        );
    }

    private RagProperties buildProps() {
        RagProperties props = new RagProperties();
        RagProperties.Embedding embedding = new RagProperties.Embedding();
        RagProperties.Embedding.Pgvector pg = new RagProperties.Embedding.Pgvector();
        pg.setDimensions(3);
        embedding.setPgvector(pg);
        props.setEmbedding(embedding);

        RagProperties.Llm llm = new RagProperties.Llm();
        llm.setPrompt("{query} {context}");
        RagProperties.Llm.Ollama ollama = new RagProperties.Llm.Ollama();
        ollama.setBaseUrl("http://localhost");
        ollama.setModel("foo");
        llm.setOllama(ollama);
        props.setLlm(llm);

        RagProperties.Inference inf = new RagProperties.Inference();
        inf.setMaxResults(1);
        inf.setMinScore(0.1);
        props.setInference(inf);
        return props;
    }

    @Test
    void queryReturnsAnswerWhenMatchesFound() {
        RagQueryRequest request = new RagQueryRequest();
        request.setQuery("hola");

        Embedding embedding = new Embedding(new float[]{0f,0f,0f});
        when(embeddingGenerator.generateEmbedding("hola")).thenReturn(embedding);
        EmbeddingMatch<String> match = new EmbeddingMatch<>(0.9, "id", embedding, "texto");
        when(embeddingStorage.findSimilar(eq(embedding), anyInt(), anyDouble())).thenReturn(List.of(match));
        when(chatMemoryService.getMessages(anyString())).thenReturn(Collections.emptyList());
        when(llmProvider.generateResponse(anyList(), anyString())).thenReturn("respuesta");

        QueryResponse response = service.query(request);

        assertEquals("respuesta", response.getAnswer());
        assertTrue(response.getConversationId() != null && !response.getConversationId().isBlank());
        assertNotNull(response.getMatches());
        assertEquals(1, response.getMatches().size());
    }

    @Test
    void queryReturnsDefaultWhenNoMatches() {
        RagQueryRequest request = new RagQueryRequest();
        request.setQuery("hola");

        Embedding embedding = new Embedding(new float[]{0f,0f,0f});
        when(embeddingGenerator.generateEmbedding("hola")).thenReturn(embedding);
        when(embeddingStorage.findSimilar(eq(embedding), anyInt(), anyDouble())).thenReturn(Collections.emptyList());
        when(chatMemoryService.getMessages(anyString())).thenReturn(Collections.emptyList());

        QueryResponse response = service.query(request);

        assertEquals("No hay información suficiente para responder a esa pregunta.", response.getAnswer());
        assertNull(response.getMatches());
    }
}
