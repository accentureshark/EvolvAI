package org.shark.evolvai.inference.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.data.document.Metadata;
import org.shark.evolvai.chathistory.port.in.ChatMemoryService;
import org.shark.evolvai.config.RagProperties;
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
        return doRagQuery(request, false);
    }

    @Override
    public QueryResponse advancedQuery(RagQueryRequest request) {
        log.info("Iniciando consulta avanzada: {}", request.getQuery());
        return doRagQuery(request, true);
    }

    private QueryResponse doRagQuery(RagQueryRequest request, boolean isAdvanced) {
        Embedding queryEmbedding = embeddingGenerator.generateEmbedding(request.getQuery());

        Map<String, String> baseFilter = Optional.ofNullable(request.getContextMetadata())
                .map(ctx -> ctx.entrySet().stream()
                        .filter(e -> e.getValue() != null)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> String.valueOf(e.getValue())
                        )))
                .orElse(Collections.emptyMap());

        Metadata filterMetadata = Metadata.from(baseFilter);
        log.info("Metadata usada para filtrar embeddings: {}", baseFilter);

        List<EmbeddingMatch<String>> matches = embeddingStorage.findSimilar(
                queryEmbedding,
                isAdvanced ? request.getMaxResults() : ragProperties.getInference().getMaxResults(),
                isAdvanced ? request.getMinSimilarity() : ragProperties.getInference().getMinScore(),
                filterMetadata
        );

        log.info("Se encontraron {} documentos similares.", matches.size());

        String context = matches.stream()
                .map(EmbeddingMatch::embedded)
                .reduce("", (a, b) -> a + "\n" + b);

        String conversationId = Optional.ofNullable(request.getConversationId())
                .filter(id -> !id.isBlank())
                .orElseGet(() -> {
                    String newId = UUID.randomUUID().toString();
                    log.debug("No se proporcionó conversationId, se genera uno nuevo: {}", newId);
                    return newId;
                });

        List<ChatMessage> conversationHistory = chatMemoryService.getMessages(conversationId);

        String prompt = Optional.ofNullable(request.getCustomPrompt())
                .filter(p -> !p.isBlank())
                .orElseGet(() -> ragProperties.getPrompt().toString() );

        String answer;

        if (matches.isEmpty()) {
            answer = "No hay información suficiente para responder a esa pregunta.";
            log.info("Sin contexto relevante. Se responde con fallback.");
        } else {
            answer = llmProvider.generateResponse(
                    conversationHistory,
                    request.getQuery(),
                    prompt
            );
        }


        log.info("Respuesta generada: {}", answer);

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

        log.info("Consulta {} finalizada. includeMatches={}", isAdvanced ? "avanzada" : "básica", request.isIncludeMatches());

        return new QueryResponse(
                answer,
                request.isIncludeMatches() ? dtos : null,
                conversationId
        );
    }
}
