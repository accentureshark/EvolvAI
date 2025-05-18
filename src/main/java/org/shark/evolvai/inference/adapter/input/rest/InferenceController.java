package org.shark.evolvai.inference.adapter.input.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.controller.QueryResponse;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.port.input.InferenceUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/inference")
@Tag(name = "RAG API", description = "API para consultas con Generación Aumentada por Recuperación")
@Validated
public class InferenceController {

    private final InferenceUseCase inferenceUseCase;

    public InferenceController(InferenceUseCase inferenceUseCase) {
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
    public ResponseEntity<QueryResponse> query(
            @Parameter(description = "Datos de la consulta", required = true)
            @Valid @RequestBody RagQueryRequest request) {
        QueryResponse response = inferenceUseCase.query(request);
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
    public ResponseEntity<QueryResponse> advancedQuery(
            @Parameter(description = "Datos de la consulta avanzada", required = true)
            @Valid @RequestBody RagQueryRequest request) {
        QueryResponse response = inferenceUseCase.advancedQuery(request);
        return ResponseEntity.ok(response);
    }
}