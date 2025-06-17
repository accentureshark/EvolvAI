package org.shark.evolvai.inference.adapter.in.datasource;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.data.document.Metadata;
import org.shark.evolvai.chathistory.port.in.ChatMemoryService;
import org.shark.evolvai.embedding.port.out.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.out.EmbeddingStorage;
import org.shark.evolvai.inference.controller.EmbeddingMatchDto;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.model.DataSourceInfo;
import org.shark.evolvai.inference.model.EmbeddingContext;
import org.shark.evolvai.inference.model.SourceType;
import org.shark.evolvai.inference.port.DataSourceHandler;
import org.shark.evolvai.inference.util.EnrichmentUtil;
import org.shark.evolvai.util.MetadataBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handler para consultas basadas en Vector DB.
 */
@Component
public class VectorDbHandler implements DataSourceHandler {

    private final EmbeddingGenerator embeddingGenerator;
    private final EmbeddingStorage embeddingStorage;
    private final ChatMemoryService chatMemoryService;

    public VectorDbHandler(EmbeddingGenerator embeddingGenerator,
                           EmbeddingStorage embeddingStorage,
                           ChatMemoryService chatMemoryService) {
        this.embeddingGenerator = embeddingGenerator;
        this.embeddingStorage = embeddingStorage;
        this.chatMemoryService = chatMemoryService;
    }

    @Override
    public boolean supports(SourceType type) {
        return type == SourceType.VECTOR_DB;
    }

    @Override
    public EmbeddingContext prepareContext(RagQueryRequest request) {
        String query = request.getQuery();
        Embedding queryEmbedding = embeddingGenerator.generateEmbedding(query);

        // Construcción unificada de metadata para el filtro
        DataSourceInfo source = request.getSource();
        Metadata filterMetadata = MetadataBuilder.fromDataSourceInfo(source);

        int maxResults = request.getMaxResults();
        double minScore = request.getMinSimilarity();

        List<EmbeddingMatch<String>> matches = embeddingStorage.findSimilar(
                queryEmbedding,
                maxResults,
                minScore,
                filterMetadata
        );

        List<EmbeddingMatchDto> matchDtos = matches.stream()
                .map(m -> new EmbeddingMatchDto(
                        m.score(),
                        m.embeddingId(),
                        m.embedding().vector(),
                        m.embedded(),
                        EnrichmentUtil.extractMetadata(m)
                ))
                .collect(Collectors.toList());

        String enrichedQueryWithContext = EnrichmentUtil.smartEnrichQuery(query, matchDtos);
        String context = EnrichmentUtil.rebuildContextFromMatches(matchDtos);

        String conversationId = Optional.ofNullable(request.getConversationId())
                .filter(id -> !id.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());

        List<ChatMessage> conversationHistory = chatMemoryService.getMessages(conversationId);

        return new EmbeddingContext(
                query,
                enrichedQueryWithContext,
                context,
                matchDtos,
                conversationId,
                conversationHistory
        );
    }
}
