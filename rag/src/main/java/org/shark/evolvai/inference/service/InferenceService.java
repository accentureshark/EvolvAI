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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

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
    private final WebClient webClient;

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

        String baseUrl = ragProperties.getLlm().getOllama().getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.webClient = WebClient.create(baseUrl);
    }

    private record EmbeddingContext(
            String enrichedQuery,
            String enrichedQueryWithContext,
            String context,
            List<EmbeddingMatchDto> matchDtos,
            String conversationId,
            List<ChatMessage> conversationHistory
    ) {}

    @Override
    public QueryResponse query(RagQueryRequest request) {
        log.info("Iniciando consulta básica: {}", request );

        EmbeddingContext ctx = prepareEmbeddingContext(request);

        if (ctx.matchDtos().isEmpty()) {
            return new QueryResponse("No hay información suficiente para responder a esa pregunta.", null, ctx.conversationId());
        }

        String promptTemplate = Optional.ofNullable(request.getCustomPrompt()).orElse(ragProperties.getLlm().getPrompt());

        // Reemplazo manual de placeholders
        String finalPrompt = promptTemplate.replace("{query}", request.getQuery())
                .replace("{context}", ctx.context());

        log.info("Prompt final enviado a LLM:\n{}", finalPrompt);

        String answer = llmProvider.generateResponse(ctx.conversationHistory(), finalPrompt);

        chatMemoryService.updateMessages(ctx.conversationId(), List.of(
                new UserMessage(request.getQuery()),
                new AiMessage(answer)
        ));

        return new QueryResponse(answer, request.isIncludeMatches() ? ctx.matchDtos() : null, ctx.conversationId());
    }

    @Override
    public QueryResponse advancedQuery(RagQueryRequest request) {
        log.info("Iniciando consulta avanzada: {}", request.getQuery());
        return query(request);
    }

    @Override
    public Flux<String> queryStream(RagQueryRequest request) {
        log.info("Iniciando consulta streaming: {}", request.getQuery());

        EmbeddingContext ctx = prepareEmbeddingContext(request);

        if (ctx.matchDtos().isEmpty()) {
            return Flux.just("No hay información suficiente para responder a esa pregunta.");
        }

        Map<String, Object> body = new HashMap<>();
        var ollama = ragProperties.getLlm().getOllama();

        body.put("model", ollama.getModel());

        String promptTemplate = Optional.ofNullable(request.getCustomPrompt()).orElse(ragProperties.getLlm().getPrompt());
        String combinedPrompt = promptTemplate.replace("{query}", request.getQuery())
                .replace("{context}", ctx.context());

        log.info("Prompt combinado enviado a Ollama (streaming):\n{}", combinedPrompt);

        body.put("prompt", combinedPrompt);
        body.put("stream", true);

        if (ollama.getTemperature() > 0) {
            body.put("temperature", ollama.getTemperature());
        }

        return webClient.post()
                .uri("/api/generate")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnSubscribe(sub -> log.info("Enviando request streaming a Ollama"))
                .doOnNext(chunk -> log.debug("Chunk recibido: {}", chunk))
                .doOnError(e -> log.error("Error en streaming Ollama", e))
                .transform(this::groupFragmentsForUi);
    }

    private EmbeddingContext prepareEmbeddingContext(RagQueryRequest request) {
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
        log.info("Vector embedding consulta (primeros 5 valores): {}", Arrays.toString(Arrays.copyOf(queryEmbedding.vector(), Math.min(5, queryEmbedding.vector().length))));

        int actualDimensions = queryEmbedding.vector().length;
        int expectedDimensions = ragProperties.getEmbedding().getPgvector().getDimensions();

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

        log.info("Usando metadata para búsqueda de embeddings: {}", contextMap);

        if ((contextMap != null && !contextMap.isEmpty()) ||
                (request.getDocumentId() != null && !request.getDocumentId().isBlank())) {

            Map<String, Object> combinedMap = new HashMap<>();
            if (contextMap != null) {
                combinedMap.putAll(contextMap);
            }
            if (request.getDocumentId() != null && !request.getDocumentId().isBlank()) {
                combinedMap.put("documentId", request.getDocumentId());
            }
            Metadata contextMetadata = Metadata.from(combinedMap);

            log.info("Realizando búsqueda findSimilar con filtro: {}", contextMetadata);

            matches = embeddingStorage.findSimilar(queryEmbedding,
                    ragProperties.getInference().getMaxResults(),
                    ragProperties.getInference().getMinScore(),
                    contextMetadata);
        } else {
            log.info("Realizando búsqueda findSimilar sin filtro");
            matches = embeddingStorage.findSimilar(queryEmbedding,
                    ragProperties.getInference().getMaxResults(),
                    ragProperties.getInference().getMinScore());
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

        enrichedQuery = EnrichmentUtil.smartEnrichQuery(request.getQuery(), matchDtos);
        String context = EnrichmentUtil.rebuildContextFromMatches(matchDtos);
        log.info("Contexto generado para el prompt:\n{}", context);

        String conversationId = Optional.ofNullable(request.getConversationId())
                .filter(id -> !id.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());

        List<ChatMessage> conversationHistory = chatMemoryService.getMessages(conversationId);

        String enrichedQueryWithContext = enrichedQuery + "\n\nContexto relevante:\n" + context;
        log.info("Query enriquecida con contexto embebido:\n{}", enrichedQueryWithContext);

        return new EmbeddingContext(
                enrichedQuery,
                enrichedQueryWithContext,
                context,
                matchDtos,
                conversationId,
                conversationHistory
        );
    }

    /**
     * @todo: Migrar a otra clase
     * @param incoming
     * @return
     */
    private Flux<String> groupFragmentsForUi(Flux<String> incoming) {
        return Flux.create(sink -> {
            StringBuilder buffer = new StringBuilder();
            incoming.subscribe(
                    chunk -> {
                        String token = extractResponseChunk(chunk);
                        if (token == null || token.isEmpty()) return;

                        buffer.append(token);

                        if (buffer.length() > 0 && (
                                Character.isWhitespace(buffer.charAt(buffer.length() - 1))
                                        || ".!?,;:".indexOf(buffer.charAt(buffer.length() - 1)) >= 0
                                        || buffer.toString().endsWith("\\n")
                                        || buffer.toString().endsWith("\n")
                        )) {
                            sink.next(buffer.toString());
                            buffer.setLength(0);
                        }
                    },
                    sink::error,
                    () -> {
                        if (buffer.length() > 0) sink.next(buffer.toString());
                        sink.complete();
                    }
            );
        });
    }

    private String extractResponseChunk(String chunk) {
        int start = chunk.indexOf("\"response\":\"");
        if (start == -1) {
            log.warn("No se encontró campo 'response' en chunk: {}", chunk);
            return "";
        }
        start += 12;
        int end = chunk.indexOf("\"", start);
        return chunk.substring(start, end);
    }
}
