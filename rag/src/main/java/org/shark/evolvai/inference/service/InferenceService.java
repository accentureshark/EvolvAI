package org.shark.evolvai.inference.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.shark.evolvai.chathistory.port.in.ChatMemoryService;
import org.shark.evolvai.config.RagProperties;
import org.shark.evolvai.embedding.port.out.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.out.EmbeddingStorage;
import org.shark.evolvai.inference.controller.EmbeddingMatchDto;
import org.shark.evolvai.inference.controller.QueryResponse;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.port.input.InferenceUseCase;
import org.shark.evolvai.inference.util.EnrichmentUtil;
import org.shark.evolvai.llm.port.out.LlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
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
        log.info("Query original: {}", request);
        return doRagQuery(request, false);
    }

    @Override
    public QueryResponse advancedQuery(RagQueryRequest request) {
        log.info("Iniciando consulta avanzada: {}", request.getQuery());
        return doRagQuery(request, true);
    }

    private QueryResponse doRagQuery(RagQueryRequest request, boolean isAdvanced) {
        String enrichedQuery = request.getQuery();

        if (request.getContextMetadata() == null && request.getDocumentId() != null) {
            Optional<Map<String, Object>> maybeMetadata = embeddingStorage.findMetadataByDocumentId(request.getDocumentId());
            if (maybeMetadata.isPresent()) {
                Map<String, String> contextMap = new HashMap<>();
                maybeMetadata.get().forEach((k, v) -> {
                    if (v != null) {
                        contextMap.put(k, v.toString());
                    }
                });
                request.setContextMetadata(contextMap);
                log.info("Context metadata extraída desde PgVector: {}", contextMap);
            }
        }

        Embedding queryEmbedding = embeddingGenerator.generateEmbedding(enrichedQuery);
        int actualDimensions = queryEmbedding.vector().length;
        int expectedDimensions = ragProperties.getEmbedding().getPgvector().getDimensions();

        log.info("Dimensión embedding consulta: {}, esperada: {}", actualDimensions, expectedDimensions);

        if (actualDimensions < expectedDimensions) {
            float[] padded = Arrays.copyOf(queryEmbedding.vector(), expectedDimensions);
            queryEmbedding = new Embedding(padded);
            log.info("Query embedding padded a {} dimensiones", expectedDimensions);
        } else if (actualDimensions > expectedDimensions) {
            log.error("El embedding generado tiene más dimensiones ({}) que las esperadas ({}). Verificá el modelo configurado.", actualDimensions, expectedDimensions);
            throw new IllegalArgumentException("Dimensiones del embedding incompatibles con configuración de pgvector.");
        }

        List<EmbeddingMatch<String>> matches;
        Map<String, String> contextMap = request.getContextMetadata();

        if ((contextMap != null && !contextMap.isEmpty()) ||
                (request.getDocumentId() != null && !request.getDocumentId().isBlank())) {

            Map<String, Object> combinedMap = new HashMap<>();
            if (contextMap != null) {
                contextMap.forEach(combinedMap::put);
            }
            if (request.getDocumentId() != null && !request.getDocumentId().isBlank()) {
                combinedMap.put("documentId", request.getDocumentId());
            }
            Metadata contextMetadata = Metadata.from(combinedMap);

            matches = embeddingStorage.findSimilar(queryEmbedding,
                    isAdvanced ? request.getMaxResults() : ragProperties.getInference().getMaxResults(),
                    isAdvanced ? request.getMinSimilarity() : ragProperties.getInference().getMinScore(),
                    contextMetadata);
        } else {
            matches = embeddingStorage.findSimilar(queryEmbedding,
                    isAdvanced ? request.getMaxResults() : ragProperties.getInference().getMaxResults(),
                    isAdvanced ? request.getMinSimilarity() : ragProperties.getInference().getMinScore());
        }

        log.info("Se encontraron {} embeddings similares.", matches.size());
        matches.stream().limit(3).forEach(m -> log.debug("Match: score={}, id={}, text={}", m.score(), m.embeddingId(), m.embedded().substring(0, Math.min(m.embedded().length(), 100))));

        List<EmbeddingMatchDto> matchDtos = matches.stream()
                .map(m -> new EmbeddingMatchDto(
                        m.score(),
                        m.embeddingId(),
                        m.embedding().vector(),
                        m.embedded(),
                        EnrichmentUtil.extractMetadata(m)
                ))
                .collect(Collectors.toList());

        if (matchDtos.isEmpty()) {
            log.warn("No se encontraron documentos relevantes para la query: {}", request.getQuery());
            return new QueryResponse("No hay información suficiente para responder a esa pregunta.", null, request.getConversationId());
        }

        enrichedQuery = EnrichmentUtil.smartEnrichQuery(request.getQuery(), matchDtos);
        String context = EnrichmentUtil.rebuildContextFromMatches(matchDtos);
        log.info("Contexto generado para el prompt:\n{}", context);

        String conversationId = Optional.ofNullable(request.getConversationId())
                .filter(id -> !id.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());

        List<ChatMessage> conversationHistory = chatMemoryService.getMessages(conversationId);

        String enrichedQueryWithContext = enrichedQuery + "\n\nContexto relevante:\n" + context;
        log.info("Query enriquecida con contexto embebido:\n{}", enrichedQueryWithContext);

        String answer = llmProvider.generateResponse(conversationHistory, enrichedQueryWithContext, null);

        chatMemoryService.updateMessages(conversationId, List.of(
                new UserMessage(request.getQuery()),
                new AiMessage(answer)
        ));

        return new QueryResponse(answer, request.isIncludeMatches() ? matchDtos : null, conversationId);
    }
}
