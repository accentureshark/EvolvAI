package org.shark.evolvai.inference.port.out;

import dev.langchain4j.data.embedding.Embedding;
import org.shark.evolvai.inference.controller.EmbeddingMatchDto;
import org.shark.evolvai.inference.controller.RagQueryRequest;

import java.util.List;

/**
 * Abstrae la obtención de coincidencias (chunks de contexto) para la inferencia.
 * Permite implementar diferentes orígenes, como PgVector o servidores externos
 * tipo MCP.
 */
public interface ContextSource {
    List<EmbeddingMatchDto> fetchMatches(Embedding queryEmbedding, RagQueryRequest request);
}
