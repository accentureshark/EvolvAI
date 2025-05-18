package org.shark.evolvai.inference.port.input;

import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.controller.QueryResponse;
import org.shark.evolvai.inference.controller.RagQueryRequest;

public interface InferenceUseCase {
    QueryResponse query(RagQueryRequest request);
    QueryResponse advancedQuery(RagQueryRequest request);
}