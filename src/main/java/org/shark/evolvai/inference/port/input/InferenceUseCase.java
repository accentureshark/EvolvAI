package org.shark.evolvai.inference.port.input;

import org.shark.evolvai.inference.controller.QueryRequest;
import org.shark.evolvai.inference.controller.QueryResponse;

public interface InferenceUseCase {
    QueryResponse query(QueryRequest request);
}
