package org.shark.evolvai.embedding.adapter.in.rest;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.shark.evolvai.config.RagProperties;
import org.shark.evolvai.embedding.port.in.EmbeddingUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.segment.TextSegment;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.shark.evolvai.embedding.domain.service.SmartChunkingService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingController {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingController.class);
    private final EmbeddingUseCase embeddingUseCase;
    private final SmartChunkingService smartChunkingService;
    private final HuggingFaceTokenizer tokenizer;
    private final RagProperties ragProperties;


    public EmbeddingController(
            EmbeddingUseCase embeddingUseCase,
            SmartChunkingService smartChunkingService,
            HuggingFaceTokenizer tokenizer,
            RagProperties ragProperties
    ) {
        this.embeddingUseCase = embeddingUseCase;
        this.smartChunkingService= smartChunkingService;
        this.tokenizer = tokenizer;
        this.ragProperties = ragProperties;
    }


    @Operation(
            summary = "Sube un documento para indexar",
            description = "Permite subir archivos .txt, .pdf o .json estructurados para ser indexados y embebidos en la base de datos de embeddings.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Documento procesado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida o tipo de archivo no soportado")
            }
    )

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
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

    @PostMapping("/embed-file")
    public ResponseEntity<?> embedFile(@RequestParam("file") MultipartFile file) {
        File tempFile = null;
        try {
            log.info("Recibiendo archivo: {}", file.getOriginalFilename());
            tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);

            Document doc = FileSystemDocumentLoader.loadDocument(tempFile.getAbsolutePath());
            String cleanText = doc.text().replaceAll("[^\\x20-\\x7E\\n]", "");

            var encoding = tokenizer.encode(cleanText);
            int[] inputIds = Arrays.stream(encoding.getIds()).mapToInt(i -> (int) i).toArray();
            int[] attentionMask = Arrays.stream(encoding.getAttentionMask()).mapToInt(i -> (int) i).toArray();

            var embedding = embeddingUseCase.generateEmbedding(inputIds, attentionMask);
            log.info("Archivo procesado exitosamente: {}", file.getOriginalFilename());

            return ResponseEntity.ok(embedding.vectorAsList());

        } catch (Exception e) {
            log.error("Error procesando archivo", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @PostMapping("/index")
    public ResponseEntity<Void> indexDocument(@RequestBody DocumentRequest request) {
        String id = request.getId() != null ? request.getId() : UUID.randomUUID().toString();
        embeddingUseCase.indexDocument(id, request.getText(), request.getCustomPrompt());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<String>> search(
            @RequestParam String query,
            @RequestParam(required = false) Integer maxResults,
            @RequestParam(required = false) Double minScore) {
        int results = maxResults != null ? maxResults : ragProperties.getInference().getMaxResults();
        double score = minScore != null ? minScore : ragProperties.getInference().getMinScore();
        List<String> resultsList = embeddingUseCase.findSimilarDocuments(query, results, score);
        return ResponseEntity.ok(resultsList);
    }

    @GetMapping("/documents")
    public ResponseEntity<List<String>> listDocuments() {
        log.info("Recibida solicitud para listar document_id distintos.");
        List<String> ids = embeddingUseCase.listDocumentIds();
        log.info("Se encontraron {} document_id distintos.", ids.size());
        return ResponseEntity.ok(ids);
    }
}