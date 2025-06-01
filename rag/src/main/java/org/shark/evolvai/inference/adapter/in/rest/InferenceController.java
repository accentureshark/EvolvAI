package org.shark.evolvai.inference.adapter.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.shark.evolvai.inference.controller.QueryResponse;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.port.input.InferenceUseCase;
import org.shark.evolvai.metrics.MonitoredEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inference")
@Tag(name = "RAG API", description = "API para consultas con Generación Aumentada por Recuperación")
@Validated
public class InferenceController {

    private static final Logger log = LoggerFactory.getLogger(InferenceController.class);
    private final InferenceUseCase inferenceUseCase;


    public InferenceController(
            InferenceUseCase inferenceUseCase

    ) {
        this.inferenceUseCase = inferenceUseCase;

    }


    @PostMapping("/query")
    @Operation(
            summary = "Consulta RAG básica",
            description = "Realiza una consulta simple usando RAG con configuración predeterminada",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Consulta procesada correctamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = QueryResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
            }
    )
    @MonitoredEndpoint(name = "api.inference.query" )
    public ResponseEntity<QueryResponse> query(
            @Parameter(description = "Datos de la consulta", required = true)
            @Valid @RequestBody RagQueryRequest request) {
        log.info("Recibida consulta RAG básica: {}", request.getQuery());
        log.info("Recibida consulta RAG documentId : {}", request.getDocumentId());
        QueryResponse response = inferenceUseCase.query(request);
        log.info("Respuesta generada para consulta RAG básica: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rag")
    @Operation(
            summary = "Consulta RAG avanzada",
            description = "Realiza una consulta RAG con opciones de configuración avanzadas",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Consulta procesada correctamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = QueryResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
            }
    )
    @MonitoredEndpoint(name = "api.inference.advancedQuery" )
    public ResponseEntity<QueryResponse> advancedQuery(
            @Parameter(description = "Datos de la consulta avanzada", required = true)
            @Valid @RequestBody RagQueryRequest request) {
        log.info("Recibida consulta RAG avanzada: {}", request.getQuery());
        QueryResponse response = inferenceUseCase.advancedQuery(request);
        log.info("Respuesta generada para consulta RAG avanzada: {}", response.getAnswer().toLowerCase());
        return ResponseEntity.ok(response);
    }
}