package org.shark.evolvai.inference.adapter.in.rest;

import org.shark.evolvai.config.RagProperties;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.metrics.MonitoredEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inference")
public class InferenceStreamController {

    private static final Logger log = LoggerFactory.getLogger(InferenceStreamController.class);

    private final RagProperties ragProperties;
    private final WebClient webClient;
    private final String endpoint;

    @Autowired
    public InferenceStreamController(RagProperties ragProperties) {
        this.ragProperties = ragProperties;

        String baseUrl = ragProperties.getLlm().getOllama().getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.webClient = WebClient.create(baseUrl);
        this.endpoint = "/api/generate";
        log.info("InferenceStreamController inicializado con baseUrl: {} y endpoint: {}", baseUrl, this.endpoint);
    }

    @PostMapping(value = "/query-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @MonitoredEndpoint(name = "api.inference.query-stream" )
    public Flux<String> queryStream(@RequestBody RagQueryRequest request) {
        var ollama = ragProperties.getLlm().getOllama();
        Map<String, Object> body = new HashMap<>();
        body.put("model", ollama.getModel());
        body.put("prompt", request.getQuery());
        body.put("stream", true);
        if (ollama.getTemperature() > 0) body.put("temperature", ollama.getTemperature());

        log.info("Recibida consulta stream: {}", request.getQuery());
        log.debug("Payload enviado a Ollama: {}", body);

        return webClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnSubscribe(sub -> log.info("Enviando request a Ollama endpoint: {}{}", webClient, endpoint))
                .doOnNext(chunk -> log.debug("Chunk recibido: {}", chunk))
                .doOnError(e -> log.error("Error en consulta stream a Ollama", e))
                .transform(this::groupFragmentsForUi);
    }

    // Agrupa tokens/frases antes de emitir al frontend
    private Flux<String> groupFragmentsForUi(Flux<String> incoming) {
        return Flux.create(sink -> {
            StringBuilder buffer = new StringBuilder();
            incoming.subscribe(
                    chunk -> {
                        String token = extractResponseChunk(chunk);
                        if (token == null || token.isEmpty()) return;

                        buffer.append(token);

                        // Si termina en espacio, puntuación, salto de línea, etc., emití el buffer
                        if (buffer.length() > 0 && (
                                Character.isWhitespace(buffer.charAt(buffer.length() - 1))
                                        || ".!?,;:".indexOf(buffer.charAt(buffer.length() - 1)) >= 0
                                        || buffer.toString().endsWith("\\n")
                                        || buffer.toString().endsWith("\n")
                        )) {
                            String toEmit = buffer.toString();
                            sink.next(toEmit);
                            buffer.setLength(0); // limpiar
                        }
                    },
                    sink::error,
                    () -> {
                        // Emití lo que quede en buffer al terminar
                        if (buffer.length() > 0) sink.next(buffer.toString());
                        sink.complete();
                    }
            );
        });
    }

    private String extractResponseChunk(String chunk) {
        int start = chunk.indexOf("\"response\":\"");
        if (start == -1) {
            log.warn("No se encontró campo 'response' en chunk: {}", chunk);
            return "";
        }
        start += 12;
        int end = chunk.indexOf("\"", start);
        String response = chunk.substring(start, end);
        log.debug("Extracted response chunk: {}", response);
        return response;
    }
}