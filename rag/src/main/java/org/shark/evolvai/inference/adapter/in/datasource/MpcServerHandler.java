package org.shark.evolvai.inference.adapter.in.datasource;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.segment.TextSegment;
import org.shark.evolvai.chathistory.port.in.ChatMemoryService;
import org.shark.evolvai.inference.controller.EmbeddingMatchDto;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.model.DataSourceInfo;
import org.shark.evolvai.inference.model.EmbeddingContext;
import org.shark.evolvai.inference.model.SourceType;
import org.shark.evolvai.inference.port.DataSourceHandler;
import org.shark.evolvai.inference.port.ExternalDataProvider;
import org.shark.evolvai.inference.util.EnrichmentUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handler para consultas desde un MCP Server, sin vectorización.
 */
@Component
public class MpcServerHandler implements DataSourceHandler {

    private final ExternalDataProvider externalDataProvider;
    private final ChatMemoryService chatMemoryService;

    public MpcServerHandler(ExternalDataProvider externalDataProvider,
                            ChatMemoryService chatMemoryService) {
        this.externalDataProvider = externalDataProvider;
        this.chatMemoryService = chatMemoryService;
    }

    @Override
    public boolean supports(SourceType type) {
        return type == SourceType.MCP_SERVER;
    }

    @Override
    public EmbeddingContext prepareContext(RagQueryRequest request) {
        DataSourceInfo source = request.getSource();
        String sourceId = source.getId();
        Map<String, Object> params = source.getParams();

        // Obtener segmentos de texto desde el MCP Server
        List<TextSegment> segments = externalDataProvider.fetch(
                request.getQuery(),
                sourceId,
                params
        );

        // Convertir segmentos a DTOs sin embeddings
        List<EmbeddingMatchDto> matchDtos = segments.stream()
                .map(seg -> new EmbeddingMatchDto(
                        0.0,
                        null,
                        null,
                        seg.text(),
                        seg.metadata() != null ? seg.metadata().toMap() : Map.of()
                ))
                .collect(Collectors.toList());

        // Reconstruir prompt y contexto
        String enrichedQuery = request.getQuery();
        String enrichedQueryWithContext = EnrichmentUtil.smartEnrichQuery(enrichedQuery, matchDtos);
        String context = EnrichmentUtil.rebuildContextFromMatches(matchDtos);

        // Manejar conversación e historial
        String conversationId = Optional.ofNullable(request.getConversationId())
                .filter(id -> !id.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());
        List<ChatMessage> conversationHistory = chatMemoryService.getMessages(conversationId);

        return new EmbeddingContext(
                enrichedQuery,
                enrichedQueryWithContext,
                context,
                matchDtos,
                conversationId,
                conversationHistory
        );
    }
}
