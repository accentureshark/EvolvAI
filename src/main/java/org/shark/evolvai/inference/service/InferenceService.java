package org.shark.evolvai.inference.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.shark.evolvai.chathistory.port.in.ChatMemoryService;
import org.shark.evolvai.embedding.port.output.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.shark.evolvai.inference.config.InferenceProperties;
import org.shark.evolvai.inference.controller.EmbeddingMatchDto;
import org.shark.evolvai.inference.controller.QueryResponse;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.port.input.InferenceUseCase;
import org.shark.evolvai.llm.port.out.LlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InferenceService implements InferenceUseCase {

    private static final Logger log = LoggerFactory.getLogger(InferenceService.class);

    private final EmbeddingGenerator embeddingGenerator;
    private final EmbeddingStorage embeddingStorage;
    private final LlmProvider llmProvider;
    private final ChatMemoryService chatMemoryService;

    private final InferenceProperties inferenceProperties;

    public InferenceService(
            EmbeddingGenerator embeddingGenerator,
            EmbeddingStorage embeddingStorage,
            LlmProvider llmProvider,
            ChatMemoryService chatMemoryService,
            InferenceProperties inferenceProperties
    ) {
        this.embeddingGenerator = embeddingGenerator;
        this.embeddingStorage = embeddingStorage;
        this.llmProvider = llmProvider;
        this.chatMemoryService = chatMemoryService;
        this.inferenceProperties = inferenceProperties;
    }

    @Override
    public QueryResponse query(RagQueryRequest request) {
        log.info("Iniciando consulta básica: {}", request.getQuery());

        Embedding queryEmbedding = embeddingGenerator.generateEmbedding(request.getQuery());

        List<EmbeddingMatch<String>> matches = embeddingStorage.findSimilar(
                queryEmbedding,
                inferenceProperties.getMaxResults(),
                inferenceProperties.getMinScore()
        );
        log.info("Se encontraron {} documentos similares.", matches.size());

        String context = matches.stream()
                .map(EmbeddingMatch::embedded)
                .reduce("", (a, b) -> a + "\n" + b);

        String conversationId = request.getConversationId();
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
            log.debug("No se proporcionó conversationId, se genera uno nuevo: {}", conversationId);
        }

        // Usar solo ChatMemoryService para historial y memoria
        List<ChatMessage> conversationHistory = chatMemoryService.getMessages(conversationId);

        String answer = llmProvider.generateResponse(
                conversationHistory,
                request.getQuery(),
                request.getCustomPrompt()
        );
        log.info("Respuesta generada: {}", answer);

        // Guardar historial/memoria unificada
        List<ChatMessage> updatedMessages = new ArrayList<>(conversationHistory);
        updatedMessages.add(new UserMessage(request.getQuery()));
        updatedMessages.add(new AiMessage(answer));
        chatMemoryService.updateMessages(conversationId, updatedMessages);

        List<EmbeddingMatchDto> dtos = matches.stream()
                .map(m -> new EmbeddingMatchDto(
                        m.score(),
                        m.embeddingId(),
                        m.embedding().vector(),
                        m.embedded()
                ))
                .collect(Collectors.toList());

        log.info("Consulta básica finalizada.");
        for (EmbeddingMatch<String> match : matches) {
            log.debug("Score: {}, ID: {}, Text preview: {}", match.score(), match.embeddingId(), match.embedded().substring(0, Math.min(100, match.embedded().length())));
        }

        return new QueryResponse(answer, dtos, conversationId);
    }

    @Override
    public QueryResponse advancedQuery(RagQueryRequest request) {
        log.info("Iniciando consulta avanzada: {}", request.getQuery());

        Embedding queryEmbedding = embeddingGenerator.generateEmbedding(request.getQuery());

        List<EmbeddingMatch<String>> matches = embeddingStorage.findSimilar(
                queryEmbedding,
                request.getMaxResults(),
                request.getMinSimilarity()
        );
        log.info("Se encontraron {} documentos similares.", matches.size());

        String context = matches.stream()
                .map(EmbeddingMatch::embedded)
                .reduce("", (a, b) -> a + "\n" + b);

        String conversationId = request.getConversationId();
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
            log.debug("No se proporcionó conversationId, se genera uno nuevo: {}", conversationId);
        } else {
            log.debug("Usando conversationId existente: {}", conversationId);
        }

        // Usar solo ChatMemoryService para historial y memoria
        List<ChatMessage> conversationHistory = chatMemoryService.getMessages(conversationId);

        String answer = llmProvider.generateResponse(
                conversationHistory,
                request.getQuery(),
                request.getCustomPrompt()
        );
        log.info("Respuesta generada: {}", answer);

        // Guardar historial/memoria unificada
        List<ChatMessage> updatedMessages = new ArrayList<>(conversationHistory);
        updatedMessages.add(new UserMessage(request.getQuery()));
        updatedMessages.add(new AiMessage(answer));
        chatMemoryService.updateMessages(conversationId, updatedMessages);

        List<EmbeddingMatchDto> dtos = matches.stream()
                .map(m -> new EmbeddingMatchDto(
                        m.score(),
                        m.embeddingId(),
                        m.embedding().vector(),
                        m.embedded()
                ))
                .collect(Collectors.toList());

        log.info("Consulta avanzada finalizada. includeMatches={}", request.isIncludeMatches());
        return new QueryResponse(answer,
                request.isIncludeMatches() ? dtos : null,
                conversationId);
    }
}
