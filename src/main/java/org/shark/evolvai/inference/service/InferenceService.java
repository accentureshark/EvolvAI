package org.shark.evolvai.inference.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.shark.evolvai.chathistory.port.output.ChatHistoryRepository;
import org.shark.evolvai.embedding.port.output.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.shark.evolvai.inference.controller.QueryResponse;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.port.input.InferenceUseCase;
import org.shark.evolvai.llm.port.out.LlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Servicio que implementa la lógica de inferencia para consultas básicas y avanzadas.
 * Utiliza embeddings, almacenamiento de embeddings, un proveedor de LLM y un repositorio de historial de chat.
 */
@Service
public class InferenceService implements InferenceUseCase {

    private static final Logger log = LoggerFactory.getLogger(InferenceService.class);

    private final EmbeddingGenerator embeddingGenerator;
    private final EmbeddingStorage embeddingStorage;
    private final LlmProvider llmProvider;
    private final ChatHistoryRepository chatHistoryRepository;

    /**
     * Constructor para inicializar las dependencias del servicio.
     *
     * @param embeddingGenerator Generador de embeddings para consultas.
     * @param embeddingStorage Almacenamiento de embeddings para búsqueda de similitudes.
     * @param llmProvider Proveedor de modelo de lenguaje (LLM).
     * @param chatHistoryRepository Repositorio para guardar y recuperar historial de chat.
     */
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

    /**
     * Realiza una consulta básica al sistema.
     * Genera un embedding de la consulta, busca documentos similares, y utiliza el LLM para generar una respuesta.
     * Guarda la interacción en el historial de chat.
     *
     * @param request Objeto que contiene la consulta del usuario.
     * @return Respuesta generada por el sistema, incluyendo los documentos similares encontrados.
     */
    @Override
    public QueryResponse query(RagQueryRequest request) {
        log.info("Iniciando consulta básica: {}", request.getQuery());

        // Generar embedding de la consulta
        log.debug("Generando embedding para la consulta...");
        Embedding queryEmbedding = embeddingGenerator.generateEmbedding(request.getQuery());

        // Buscar documentos similares
        log.debug("Buscando documentos similares...");
        List<EmbeddingMatch<String>> matches = embeddingStorage.findSimilar(queryEmbedding, 5, 0.7);
        log.info("Se encontraron {} documentos similares.", matches.size());

        // Concatenar textos relevantes
        String context = matches.stream()
                .map(EmbeddingMatch::embedded)
                .reduce("", (a, b) -> a + "\n" + b);

        // Generar respuesta usando el LLM
        log.debug("Generando respuesta con el LLM...");
        String answer = llmProvider.generateResponse(context, request.getQuery());
        log.info("Respuesta generada: {}", answer);

        // Guardar interacción en el historial de chat
        log.debug("Guardando interacción en el historial de chat...");
        chatHistoryRepository.saveInteraction(request.getQuery(), answer);

        // Devolver respuesta con los matches encontrados
        log.info("Consulta básica finalizada.");
        return new QueryResponse(answer, matches);
    }

    /**
     * Realiza una consulta avanzada al sistema.
     * Genera un embedding de la consulta, busca documentos similares con parámetros personalizados,
     * incluye historial de conversación si está disponible, y utiliza el LLM para generar una respuesta.
     * Guarda la interacción en el historial de chat con un ID de conversación.
     *
     * @param request Objeto que contiene la consulta avanzada del usuario.
     * @return Respuesta generada por el sistema, incluyendo los documentos similares si se solicita.
     */
    @Override
    public QueryResponse advancedQuery(RagQueryRequest request) {
        log.info("Iniciando consulta avanzada: {}", request.getQuery());

        // Generar embedding de la consulta
        log.debug("Generando embedding para la consulta avanzada...");
        Embedding queryEmbedding = embeddingGenerator.generateEmbedding(request.getQuery());

        // Buscar documentos similares con parámetros personalizados
        log.debug("Buscando documentos similares (maxResults={}, minSimilarity={})...",
                request.getMaxResults(), request.getMinSimilarity());
        List<EmbeddingMatch<String>> matches = embeddingStorage.findSimilar(
                queryEmbedding,
                request.getMaxResults(),
                request.getMinSimilarity()
        );
        log.info("Se encontraron {} documentos similares.", matches.size());

        // Concatenar textos relevantes
        String context = matches.stream()
                .map(EmbeddingMatch::embedded)
                .reduce("", (a, b) -> a + "\n" + b);

        // Obtener historial de conversación si existe ID
        String conversationId = request.getConversationId();
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
            log.debug("No se proporcionó conversationId, se genera uno nuevo: {}", conversationId);
        } else {
            log.debug("Usando conversationId existente: {}", conversationId);
        }
        log.debug("Recuperando historial de conversación...");
        String conversationHistory = chatHistoryRepository.getConversationHistory(conversationId);

        // Consultar al LLM con el contexto y el historial
        log.debug("Generando respuesta con el LLM (incluyendo historial)...");
        String answer = llmProvider.generateResponseWithHistory(context, request.getQuery(), conversationHistory);
        log.info("Respuesta generada: {}", answer);

        // Guardar en historial con ID de conversación
        log.debug("Guardando interacción en el historial de chat con conversationId...");
        chatHistoryRepository.saveInteractionWithId(conversationId, request.getQuery(), answer);

        // Devolver respuesta con o sin matches según configuración
        log.info("Consulta avanzada finalizada. includeMatches={}", request.isIncludeMatches());
        QueryResponse response = new QueryResponse(answer,
                request.isIncludeMatches() ? matches : null);
        return response;
    }
}