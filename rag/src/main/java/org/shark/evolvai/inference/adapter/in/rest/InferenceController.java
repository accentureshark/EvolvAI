package org.shark.evolvai.inference.adapter.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inference")
@Tag(name = "RAG API", description = "API para consultas con Generación Aumentada por Recuperación (RAG)")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class InferenceController {

    private static final Logger log = LoggerFactory.getLogger(InferenceController.class);
    private final InferenceUseCase inferenceUseCase;

    public InferenceController(InferenceUseCase inferenceUseCase) {
        this.inferenceUseCase = inferenceUseCase;
    }

    @PostMapping("/query")
    @Operation(
            summary = "Consulta RAG básica",
            description = """
                    Realiza una consulta simple usando RAG con configuración predeterminada.
                    Esta operación utiliza parámetros optimizados por defecto para la mayoría
                    de los casos de uso comunes.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Consulta procesada correctamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = QueryResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solicitud inválida",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Error.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "No autorizado",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "429",
                            description = "Demasiadas solicitudes",
                            content = @Content
                    )
            }
    )
    @MonitoredEndpoint(name = "api.inference.query")
    public ResponseEntity<QueryResponse> query(
            @Parameter(
                    description = """
                            Datos de la consulta básica.
                            Debe incluir al menos el campo 'query' con la pregunta a procesar.
                            """,
                    required = true,
                    schema = @Schema(implementation = RagQueryRequest.class)
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody RagQueryRequest request) {
        log.info("Recibida consulta RAG básica: {}", request.getQuery());
        log.info("Recibida consulta RAG conversationID: {}", request.getConversationId());
        log.info("Recibida consulta RAG prompt: {}", request.getCustomPrompt());
        log.info("Recibida consulta RAG documentId : {}", request.getDocumentId());
        QueryResponse response = inferenceUseCase.query(request);
        log.info("Respuesta generada para consulta RAG básica: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rag")
    @Operation(
            summary = "Consulta RAG avanzada",
            description = """
                    Realiza una consulta RAG con opciones de configuración avanzadas.
                    Permite personalizar parámetros como temperatura, número de documentos a recuperar,
                    y otros ajustes específicos del modelo.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Consulta procesada correctamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = QueryResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solicitud inválida o parámetros incorrectos",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Error.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "No autorizado",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "429",
                            description = "Demasiadas solicitudes",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor",
                            content = @Content
                    )
            }
    )
    @MonitoredEndpoint(name = "api.inference.advancedQuery")
    public ResponseEntity<QueryResponse> advancedQuery(
            @Parameter(
                    description = """
                            Datos de la consulta avanzada.
                            Incluye configuraciones adicionales como temperatura,
                            número máximo de tokens, y otros parámetros del modelo.
                            """,
                    required = true,
                    schema = @Schema(implementation = RagQueryRequest.class)
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody RagQueryRequest request) {
        log.info("Recibida consulta RAG avanzada: {}", request.getQuery());
        QueryResponse response = inferenceUseCase.advancedQuery(request);
        log.info("Respuesta generada para consulta RAG avanzada: {}", response.getAnswer().toLowerCase());
        return ResponseEntity.ok(response);
    }
}