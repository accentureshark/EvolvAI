package org.shark.evolvai.inference.port;

import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.model.SourceType;
import org.shark.evolvai.inference.model.EmbeddingContext;

/**
 * Puerto hexagonal: cada handler prepara el contexto para un tipo de fuente.
 */
public interface DataSourceHandler {

    /** Indica si este handler soporta el tipo de fuente dado. */
    boolean supports(SourceType type);

    /** Construye el EmbeddingContext (prompt+historia) para el request. */
    EmbeddingContext prepareContext(RagQueryRequest request);
}
