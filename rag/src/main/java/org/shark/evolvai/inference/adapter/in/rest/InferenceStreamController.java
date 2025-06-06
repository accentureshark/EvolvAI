package org.shark.evolvai.inference.adapter.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "RAG Streaming API", description = "API para consultas RAG con respuestas en streaming")
public class InferenceStreamController {

    private static final Logger log = LoggerFactory.getLogger(InferenceStreamController.class);

    private final InferenceUseCase inferenceUseCase;

    @Autowired
    public InferenceStreamController(InferenceUseCase inferenceUseCase) {
        this.inferenceUseCase = inferenceUseCase;
    }

    @Operation(
            summary = "Consulta RAG en streaming",
            description = "Realiza una consulta RAG y devuelve la respuesta fragmentada en tiempo real usando Server-Sent Events (SSE). ")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Stream de respuesta iniciado correctamente",
                    content = @Content(
                            mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    description = "Fragmentos de texto de la respuesta en formato SSE")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida - El cuerpo de la petición es inválido o falta información requerida"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor al procesar la consulta"
            )
    })
    @PostMapping(value = "/query-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @MonitoredEndpoint(name = "api.inference.query-stream")
    public Flux<String> queryStream(@RequestBody RagQueryRequest request) {
        log.info("Recibida consulta stream: {}", request.getQuery());
        return inferenceUseCase.queryStream(request);
    }
}
