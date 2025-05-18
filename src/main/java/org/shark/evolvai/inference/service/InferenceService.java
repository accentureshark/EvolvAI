package org.shark.evolvai.inference.service;

import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.controller.QueryResponse;
// Eliminar importación duplicada
import org.shark.evolvai.inference.port.input.InferenceUseCase;
import org.shark.evolvai.embedding.port.output.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.shark.evolvai.llm.port.out.LlmProvider;
import org.shark.evolvai.chathistory.port.output.ChatHistoryRepository;
import org.springframework.stereotype.Service;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.util.List;
import java.util.UUID;

@Service
public class InferenceService implements InferenceUseCase {

    private final EmbeddingGenerator embeddingGenerator;
    private final EmbeddingStorage embeddingStorage;
    private final LlmProvider llmProvider;
    private final ChatHistoryRepository chatHistoryRepository;

    public InferenceService(
            EmbeddingGenerator embeddingGenerator,
            EmbeddingStorage embeddingStorage,
            LlmProvider llmProvider,
            ChatHistoryRepository chatHistoryRepository
    ) {
        this.embeddingGenerator = embeddingGenerator;
        this.embeddingStorage = embeddingStorage;
        this.llmProvider = llmProvider;
        this.chatHistoryRepository = chatHistoryRepository;
    }

    @Override
    public QueryResponse query(RagQueryRequest request) {
        // Implementación existente para compatibilidad
        Embedding queryEmbedding = embeddingGenerator.generateEmbedding(request.getQuery());
        List<EmbeddingMatch<String>> matches = embeddingStorage.findSimilar(queryEmbedding, 5, 0.7);
        String context = matches.stream()
                .map(EmbeddingMatch::embedded)
                .reduce("", (a, b) -> a + "\n" + b);
        String answer = llmProvider.generateResponse(context, request.getQuery());
        chatHistoryRepository.saveInteraction(request.getQuery(), answer);
        return new QueryResponse(answer, matches);
    }

    @Override
    public QueryResponse advancedQuery(RagQueryRequest request) {
        // 1. Generar embedding de la consulta
        Embedding queryEmbedding = embeddingGenerator.generateEmbedding(request.getQuery());

        // 2. Buscar documentos similares con parámetros personalizados
        List<EmbeddingMatch<String>> matches = embeddingStorage.findSimilar(
                queryEmbedding,
                request.getMaxResults(),
                request.getMinSimilarity()
        );

        // 3. Concatenar textos relevantes
        String context = matches.stream()
                .map(EmbeddingMatch::embedded)
                .reduce("", (a, b) -> a + "\n" + b);

        // 4. Obtener historial de conversación si existe ID
        String conversationId = request.getConversationId();
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }
        String conversationHistory = chatHistoryRepository.getConversationHistory(conversationId);

        // 5. Consultar al LLM con el contexto y el historial
        String answer = llmProvider.generateResponseWithHistory(context, request.getQuery(), conversationHistory);

        // 6. Guardar en historial con ID de conversación
        chatHistoryRepository.saveInteractionWithId(conversationId, request.getQuery(), answer);

        // 7. Devolver respuesta con o sin matches según configuración
        QueryResponse response = new QueryResponse(answer,
                request.isIncludeMatches() ? matches : null);
        return response;
    }
}