package org.shark.evolvai.inference.port.input;

import org.shark.evolvai.inference.controller.QueryResponse;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import reactor.core.publisher.Flux;

public interface InferenceUseCase {
    QueryResponse query(RagQueryRequest request);

    QueryResponse advancedQuery(RagQueryRequest request);

    Flux<String> queryStream(RagQueryRequest request);  // Nuevo método para streaming
}
