package org.shark.evolvai.inference.adapter.out;

import dev.langchain4j.data.embedding.Embedding;
import org.shark.evolvai.inference.controller.EmbeddingMatchDto;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.port.out.ContextSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * Ejemplo simple de ContextSource que consulta un servidor MCP para obtener
 * chunks relevantes. Se activa solo si la propiedad "rag.mcp.enabled" es true.
 */
@Component
@ConditionalOnProperty(name = "rag.mcp.enabled", havingValue = "true")
public class McpContextSource implements ContextSource {

    private static final Logger log = LoggerFactory.getLogger(McpContextSource.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<EmbeddingMatchDto> fetchMatches(Embedding queryEmbedding, RagQueryRequest request) {
        log.info("Consultando MCP server para obtener contexto");
        // Implementación simplificada: se debería enviar el embedding y filtros
        // al servidor MCP y recibir una lista de coincidencias.
        // Al no disponer de la API exacta, se devuelve una lista vacía.
        return Collections.emptyList();
    }
}
