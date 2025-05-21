package org.shark.evolvai.inference.adapter.input.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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

@RestController
@RequestMapping("/api/inference")
@Tag(name = "RAG API", description = "API para consultas con Generación Aumentada por Recuperación")
@Validated
public class InferenceController {

    private static final Logger log = LoggerFactory.getLogger(InferenceController.class);

    private final InferenceUseCase inferenceUseCase;
    private final EmbeddingUseCase embeddingUseCase;

    public InferenceController(InferenceUseCase inferenceUseCase, EmbeddingUseCase embeddingUseCase) {
        this.inferenceUseCase = inferenceUseCase;
        this.embeddingUseCase = embeddingUseCase;
    }

    @PostMapping("/upload-document")
    @Operation(
            summary = "Sube y embebe un documento",
            description = "Permite subir un archivo .txt o .pdf, lo procesa y lo guarda en la base de embeddings",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Documento procesado y embebido correctamente"),
                    @ApiResponse(responseCode = "400", description = "Archivo inválido")
            }
    )
    public ResponseEntity<Void> uploadDocument(
            @Parameter(description = "Archivo .txt o .pdf", required = true)
            @RequestParam("file") MultipartFile file) {
        log.info("Recibida solicitud para subir documento: {}", file.getOriginalFilename());
        try {
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".txt") && !filename.endsWith(".pdf"))) {
                log.warn("Archivo inválido: {}", filename);
                return ResponseEntity.badRequest().build();
            }
            String text;
            if (filename.endsWith(".txt")) {
                log.info("Procesando archivo de texto: {}", filename);
                text = new String(file.getBytes(), StandardCharsets.UTF_8);
            } else {
                log.info("Procesando archivo PDF: {}", filename);
                try (InputStream is = file.getInputStream(); PDDocument pdf = PDDocument.load(is)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    text = stripper.getText(pdf);
                }
            }
            log.info("Indexando documento en embeddings: {}", filename);
            embeddingUseCase.indexDocument(filename, text); // <-- Aquí se invoca la segmentación
            log.info("Documento procesado y embebido correctamente: {}", filename);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error procesando el documento", e);
            return ResponseEntity.badRequest().build();
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