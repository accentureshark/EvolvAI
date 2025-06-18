package org.shark.evolvai.inference.adapter.out;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.shark.evolvai.embedding.port.out.EmbeddingStorage;
import org.shark.evolvai.inference.controller.EmbeddingMatchDto;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.port.out.ContextSource;
import org.shark.evolvai.inference.util.EnrichmentUtil;
import org.shark.evolvai.config.RagProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación de ContextSource basada en EmbeddingStorage (PgVector u otra).
 */
@Component
public class EmbeddingStoreContextSource implements ContextSource {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingStoreContextSource.class);

    private final EmbeddingStorage embeddingStorage;
    private final RagProperties ragProperties;

    public EmbeddingStoreContextSource(EmbeddingStorage embeddingStorage, RagProperties ragProperties) {
        this.embeddingStorage = embeddingStorage;
        this.ragProperties = ragProperties;
    }

    @Override
    public List<EmbeddingMatchDto> fetchMatches(Embedding queryEmbedding, RagQueryRequest request) {
        List<EmbeddingMatch<String>> matches;
        Map<String, String> contextMap = request.getContextMetadata();

        log.info("Usando EmbeddingStoreContextSource. Metadata: {}", contextMap);

        if ((contextMap != null && !contextMap.isEmpty()) ||
                (request.getDocumentId() != null && !request.getDocumentId().isBlank())) {
            Map<String, Object> combinedMap = new java.util.HashMap<>();
            if (contextMap != null) combinedMap.putAll(contextMap);
            if (request.getDocumentId() != null && !request.getDocumentId().isBlank()) {
                combinedMap.put("documentId", request.getDocumentId());
            }
            Metadata contextMetadata = Metadata.from(combinedMap);
            log.info("Realizando búsqueda con filtro: {}", contextMetadata);
            matches = embeddingStorage.findSimilar(queryEmbedding,
                    ragProperties.getInference().getMaxResults(),
                    ragProperties.getInference().getMinScore(),
                    contextMetadata);
        } else {
            log.info("Realizando búsqueda sin filtro");
            matches = embeddingStorage.findSimilar(queryEmbedding,
                    ragProperties.getInference().getMaxResults(),
                    ragProperties.getInference().getMinScore());
        }

        return matches.stream()
                .map(m -> new EmbeddingMatchDto(
                        m.score(),
                        m.embeddingId(),
                        m.embedding().vector(),
                        m.embedded(),
                        EnrichmentUtil.extractMetadata(m)
                ))
                .collect(Collectors.toList());
    }
}
