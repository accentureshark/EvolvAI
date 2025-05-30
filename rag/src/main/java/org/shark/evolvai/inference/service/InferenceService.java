package org.shark.evolvai.inference.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.data.document.Metadata;
import org.shark.evolvai.chathistory.port.in.ChatMemoryService;
import org.shark.evolvai.config.RagProperties;
import org.shark.evolvai.embedding.port.out.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.out.EmbeddingStorage;
import org.shark.evolvai.inference.controller.EmbeddingMatchDto;
import org.shark.evolvai.inference.controller.QueryResponse;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.port.input.InferenceUseCase;
import org.shark.evolvai.llm.port.out.LlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class InferenceService implements InferenceUseCase {

    private static final Logger log = LoggerFactory.getLogger(InferenceService.class);

    private final EmbeddingGenerator embeddingGenerator;
    private final EmbeddingStorage embeddingStorage;
    private final LlmProvider llmProvider;
    private final ChatMemoryService chatMemoryService;
    private final RagProperties ragProperties;

    public InferenceService(
            EmbeddingGenerator embeddingGenerator,
            EmbeddingStorage embeddingStorage,
            LlmProvider llmProvider,
            ChatMemoryService chatMemoryService,
            RagProperties ragProperties
    ) {
        this.embeddingGenerator = embeddingGenerator;
        this.embeddingStorage = embeddingStorage;
        this.llmProvider = llmProvider;
        this.chatMemoryService = chatMemoryService;
        this.ragProperties = ragProperties;
    }

    @Override
    public QueryResponse query(RagQueryRequest request) {
        log.info("Iniciando consulta básica: {}", request.getQuery());
        return doRagQuery(request, false);
    }

    @Override
    public QueryResponse advancedQuery(RagQueryRequest request) {
        log.info("Iniciando consulta avanzada: {}", request.getQuery());
        return doRagQuery(request, true);
    }

    private QueryResponse doRagQuery(RagQueryRequest request, boolean isAdvanced) {
        String enrichedQuery = enrichQueryForEmbedding(request.getQuery());
        log.info("Consulta enriquecida: {}", enrichedQuery);

        Embedding queryEmbedding = embeddingGenerator.generateEmbedding(enrichedQuery);
        log.info("Dimensión embedding consulta: {}", queryEmbedding.vector().length);

        List<EmbeddingMatch<String>> matches = embeddingStorage.findSimilar(
                queryEmbedding,
                isAdvanced ? request.getMaxResults() : ragProperties.getInference().getMaxResults(),
                isAdvanced ? request.getMinSimilarity() : ragProperties.getInference().getMinScore()
        );

        log.info("Se encontraron {} embeddings similares (sin ningún filtro).", matches.size());

        if (matches.isEmpty()) {
            return new QueryResponse("No hay información suficiente para responder a esa pregunta.", null, request.getConversationId());
        }

        String context = matches.stream()
                .map(EmbeddingMatch::embedded)
                .collect(Collectors.joining("\n"));

        String conversationId = Optional.ofNullable(request.getConversationId())
                .filter(id -> !id.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());

        List<ChatMessage> conversationHistory = chatMemoryService.getMessages(conversationId);

        String prompt = Optional.ofNullable(request.getCustomPrompt())
                .filter(p -> !p.isBlank())
                .orElseGet(() -> ragProperties.getPrompt().toString());

        String contextualPrompt = prompt + "\n\n" + context;
        String answer = llmProvider.generateResponse(conversationHistory, request.getQuery(), contextualPrompt);

        chatMemoryService.updateMessages(conversationId, List.of(
                new UserMessage(request.getQuery()),
                new AiMessage(answer)
        ));

        List<EmbeddingMatchDto> dtos = matches.stream()
                .map(m -> new EmbeddingMatchDto(m.score(), m.embeddingId(), m.embedding().vector(), m.embedded()))
                .collect(Collectors.toList());

        return new QueryResponse(answer, request.isIncludeMatches() ? dtos : null, conversationId);
    }

    private String enrichQueryForEmbedding(String original) {
        String normalized = original.toLowerCase(Locale.ROOT);

        Pattern levelPattern = Pattern.compile("level\\s*(\\d+)");
        Matcher levelMatcher = levelPattern.matcher(normalized);

        String nivel = levelMatcher.find() ? levelMatcher.group(1) : null;

        if (nivel != null && normalized.contains("tecnico")) {
            return "Nivel " + nivel + " - Área TECNICO: " + original;
        } else if (nivel != null && normalized.contains("carrera")) {
            return "Nivel " + nivel + " - Área CARRERA: " + original;
        } else {
            return original; // sin enriquecimiento
        }
    }
}
