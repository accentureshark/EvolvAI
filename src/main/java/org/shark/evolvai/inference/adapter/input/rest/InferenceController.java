package org.shark.evolvai.inference.adapter.input.rest;

import org.shark.evolvai.inference.controller.QueryRequest;
import org.shark.evolvai.inference.controller.QueryResponse;
import org.shark.evolvai.inference.port.input.InferenceUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inference")
public class InferenceController {

    private final InferenceUseCase inferenceUseCase;

    public InferenceController(InferenceUseCase inferenceUseCase) {
        this.inferenceUseCase = inferenceUseCase;
    }

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> query(@RequestBody QueryRequest request) {
        QueryResponse response = inferenceUseCase.query(request);
        return ResponseEntity.ok(response);
    }
}
