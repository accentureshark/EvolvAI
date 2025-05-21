package org.shark.evolvai.inference.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.shark.evolvai.chathistory.port.output.ChatHistoryRepository;
import org.shark.evolvai.chathistory.service.ChatMemoryService;
import org.shark.evolvai.embedding.port.output.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
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
    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatMemoryService chatMemoryService;

    public InferenceService(
            EmbeddingGenerator embeddingGenerator,
            EmbeddingStorage embeddingStorage,
            LlmProvider llmProvider,
            ChatHistoryRepository chatHistoryRepository,
            ChatMemoryService chatMemoryService
    ) {
        this.embeddingGenerator = embeddingGenerator;
        this.embeddingStorage = embeddingStorage;
        this.llmProvider = llmProvider;
        this.chatHistoryRepository = chatHistoryRepository;
        this.chatMemoryService = chatMemoryService;
    }

    @Override
    public QueryResponse query(RagQueryRequest request) {
        log.info("Iniciando consulta básica: {}", request.getQuery());

        Embedding queryEmbedding = embeddingGenerator.generateEmbedding(request.getQuery());

        List<EmbeddingMatch<String>> matches = embeddingStorage.findSimilar(queryEmbedding, 5, 0.7);
        log.info("Se encontraron {} documentos similares.", matches.size());

        String context = matches.stream()
                .map(EmbeddingMatch::embedded)
                .reduce("", (a, b) -> a + "\n" + b);

        String answer = llmProvider.generateResponse(context, request.getQuery());
        log.info("Respuesta generada: {}", answer);

        chatHistoryRepository.saveInteraction(request.getQuery(), answer);

        // Acumula mensajes previos en la memoria de chat
        String conversationId = request.getConversationId() != null ? request.getConversationId() : UUID.randomUUID().toString();
        List<ChatMessage> prevMessages = chatMemoryService.getMessages(conversationId);
        List<ChatMessage> updatedMessages = new ArrayList<>(prevMessages);
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
        return new QueryResponse(answer, dtos);
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
        log.debug("Recuperando historial de conversación...");
        String conversationHistory = chatHistoryRepository.getConversationHistory(conversationId);

        String answer = llmProvider.generateResponseWithHistory(context, request.getQuery(), conversationHistory);
        log.info("Respuesta generada: {}", answer);

        chatHistoryRepository.saveInteractionWithId(conversationId, request.getQuery(), answer);

        // Acumula mensajes previos en la memoria de chat
        List<ChatMessage> prevMessages = chatMemoryService.getMessages(conversationId);
        List<ChatMessage> updatedMessages = new ArrayList<>(prevMessages);
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
        QueryResponse response = new QueryResponse(answer,
                request.isIncludeMatches() ? dtos : null);
        return response;
    }
}