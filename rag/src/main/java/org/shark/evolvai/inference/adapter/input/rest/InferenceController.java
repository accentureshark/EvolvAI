package org.shark.evolvai.inference.adapter.input.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.segment.TextSegment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.shark.evolvai.config.RagProperties;
import org.shark.evolvai.embedding.domain.service.SmartChunkingService;
import org.shark.evolvai.embedding.port.input.EmbeddingUseCase;
import org.shark.evolvai.inference.controller.QueryResponse;
import org.shark.evolvai.inference.controller.RagQueryRequest;
import org.shark.evolvai.inference.port.input.InferenceUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/inference")
@Tag(name = "RAG API", description = "API para consultas con Generación Aumentada por Recuperación")
@Validated
public class InferenceController {

    private static final Logger log = LoggerFactory.getLogger(InferenceController.class);

    private final InferenceUseCase inferenceUseCase;
    private final EmbeddingUseCase embeddingUseCase;
    private final RagProperties ragProperties;
    private final SmartChunkingService smartChunkingService;

    public InferenceController(
            InferenceUseCase inferenceUseCase,
            EmbeddingUseCase embeddingUseCase,
            RagProperties ragProperties,
            SmartChunkingService smartChunkingService
    ) {
        this.inferenceUseCase = inferenceUseCase;
        this.embeddingUseCase = embeddingUseCase;
        this.ragProperties = ragProperties;
        this.smartChunkingService = smartChunkingService;
    }

    @PostMapping("/upload")
    @Operation(
            summary = "Sube un documento para indexar",
            description = "Permite subir archivos .txt, .pdf o .json estructurados para ser indexados y embebidos en la base de datos de embeddings.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Documento procesado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida o tipo de archivo no soportado")
            }
    )
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        log.info("Recibida solicitud para subir documento: {}", file.getOriginalFilename());
        try {
            String filename = file.getOriginalFilename();
            if (filename == null) {
                log.warn("Archivo inválido: nombre nulo");
                return ResponseEntity.badRequest().build();
            }

            Map<String, String> baseMetadata = new HashMap<>();
            String docId = filename;

            Object input;

            if (filename.endsWith(".json")) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> doc = mapper.readValue(file.getInputStream(), new TypeReference<>() {});
                baseMetadata.putAll((Map<String, String>) doc.get("metadata"));
                docId = baseMetadata.getOrDefault("documentId", filename);
                input = doc.get("data");
            } else {
                String text;
                if (filename.endsWith(".txt")) {
                    log.info("Procesando archivo de texto: {}", filename);
                    text = new String(file.getBytes(), StandardCharsets.UTF_8);
                } else if (filename.endsWith(".pdf")) {
                    log.info("Procesando archivo PDF: {}", filename);
                    try (InputStream is = file.getInputStream(); PDDocument pdf = PDDocument.load(is)) {
                        PDFTextStripper stripper = new PDFTextStripper();
                        text = stripper.getText(pdf);
                    }
                } else {
                    log.warn("Tipo de archivo no soportado: {}", filename);
                    return ResponseEntity.badRequest().body("Tipo de archivo no soportado.");
                }

                if (text == null || text.trim().isBlank()) {
                    log.warn("El archivo {} está vacío o no contiene texto procesable.", filename);
                    return ResponseEntity.badRequest().body("El archivo está vacío o no contiene texto procesable.");
                }
                input = text;
                baseMetadata.put("documentId", filename);
            }

            String prompt = Optional.ofNullable(baseMetadata.get("customPrompt"))
                    .filter(p -> !p.isBlank())
                    .or(() -> Optional.ofNullable(ragProperties.getLlm())
                            .map(r -> r.getPrompt())
                            .filter(p -> !p.isBlank()))
                    .orElse("Por favor responde de forma clara y profesional.");

            List<TextSegment> chunks = smartChunkingService.chunk(input, baseMetadata);

            if (chunks.isEmpty()) {
                log.warn("No se generaron chunks para el documento {}", docId);
                return ResponseEntity.badRequest().body("No se generaron fragmentos útiles del documento.");
            }

            embeddingUseCase.index(chunks);
            log.info("Documento {} indexado correctamente con {} fragmentos.", docId, chunks.size());

            return ResponseEntity.ok("Documento procesado correctamente con " + chunks.size() + " fragmentos.");
        } catch (Exception e) {
            log.error("Error procesando el documento", e);
            return ResponseEntity.status(500).body("Error procesando el documento.");
        }
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
        log.info("Recibida consulta RAG básica: {}", request.getQuery());
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
    public ResponseEntity<QueryResponse> advancedQuery(
            @Parameter(description = "Datos de la consulta avanzada", required = true)
            @Valid @RequestBody RagQueryRequest request) {
        log.info("Recibida consulta RAG avanzada: {}", request.getQuery());
        QueryResponse response = inferenceUseCase.advancedQuery(request);
        log.info("Respuesta generada para consulta RAG avanzada: {}", response.getAnswer().toLowerCase());
        return ResponseEntity.ok(response);
    }
}