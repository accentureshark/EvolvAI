package org.shark.evolvai.inference.adapter.in.rest;

import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.port.input.InferenceUseCase;
import org.shark.evolvai.metrics.MonitoredEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/inference")
public class InferenceStreamController {

    private static final Logger log = LoggerFactory.getLogger(InferenceStreamController.class);

    private final InferenceUseCase inferenceUseCase;

    @Autowired
    public InferenceStreamController(InferenceUseCase inferenceUseCase) {
        this.inferenceUseCase = inferenceUseCase;
    }

    @PostMapping(value = "/query-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @MonitoredEndpoint(name = "api.inference.query-stream")
    public Flux<String> queryStream(@RequestBody RagQueryRequest request) {
        log.info("Recibida consulta stream: {}", request.getQuery());
        return inferenceUseCase.queryStream(request);
    }
}
