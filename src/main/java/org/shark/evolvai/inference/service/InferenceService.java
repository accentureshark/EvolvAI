package org.shark.evolvai.inference.service;

import org.shark.evolvai.inference.controller.QueryRequest;
import org.shark.evolvai.inference.controller.QueryResponse;
import org.shark.evolvai.inference.port.input.InferenceUseCase;
import org.shark.evolvai.embedding.port.output.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.shark.evolvai.llm.port.out.LlmProvider;
import org.shark.evolvai.chathistory.port.output.ChatHistoryRepository;
import org.springframework.stereotype.Service;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.util.List;

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
    public QueryResponse query(QueryRequest request) {
        // 1. Generar embedding de la consulta
        Embedding queryEmbedding = embeddingGenerator.generateEmbedding(request.getQuery());

        // 2. Buscar documentos similares
        List<EmbeddingMatch<String>> matches = embeddingStorage.findSimilar(queryEmbedding, 5, 0.7);

        // 3. Concatenar textos relevantes
        String context = matches.stream()
                .map(EmbeddingMatch::embedded)
                .reduce("", (a, b) -> a + "\n" + b);

        // 4. Consultar al LLM con el contexto
        String answer = llmProvider.generateResponse(context, request.getQuery());

        // 5. Guardar en historial
        chatHistoryRepository.saveInteraction(request.getQuery(), answer);

        // 6. Devolver respuesta
        return new QueryResponse(answer, matches);
    }
}