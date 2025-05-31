package org.shark.evolvai.embedding.adapter.in.rest;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.shark.evolvai.config.RagProperties;
import org.shark.evolvai.embedding.port.in.EmbeddingUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.segment.TextSegment;

import org.shark.evolvai.embedding.domain.service.SmartChunkingService;


import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingController {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingController.class);
    private final EmbeddingUseCase embeddingUseCase;
    private final SmartChunkingService smartChunkingService;

    private final RagProperties ragProperties;


    public EmbeddingController(
            EmbeddingUseCase embeddingUseCase,
            SmartChunkingService smartChunkingService,

            RagProperties ragProperties
    ) {
        this.embeddingUseCase = embeddingUseCase;
        this.smartChunkingService= smartChunkingService;

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
            Object input= null;
            String docId ;

            // JSON estructurado
            if (filename.endsWith(".json")) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> doc = mapper.readValue(file.getInputStream(), new TypeReference<>() {});
                baseMetadata.putAll((Map<String, String>) doc.get("metadata"));
                docId = baseMetadata.getOrDefault("documentId", filename);  // Usa el que vino o el nuevo UUID
                input = doc.get("data");
            }

            // Forzar siempre documentId y originalFile al nombre del archivo subido
            baseMetadata.put("documentId", filename);
            baseMetadata.put("originalFile", filename);

            List<TextSegment> chunks = smartChunkingService.chunk(input, baseMetadata);

            if (chunks.isEmpty()) {
                log.warn("No se generaron chunks para el documento {}", filename);
                return ResponseEntity.badRequest().body("No se generaron fragmentos útiles del documento.");
            }

            embeddingUseCase.index(chunks);
            log.info("Documento {} indexado correctamente con {} fragmentos.", filename, chunks.size());

            return ResponseEntity.ok("Documento procesado correctamente con " + chunks.size() + " fragmentos.");
        } catch (Exception e) {
            log.error("Error procesando el documento", e);
            return ResponseEntity.status(500).body("Error procesando el documento.");
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

    @DeleteMapping("/remove-all")
    @Operation(
            summary = "Elimina todos los embeddings del almacenamiento",
            description = "Borra todos los embeddings actualmente almacenados en PgVector u otro backend configurado."
    )
    public ResponseEntity<String> removeAllEmbeddings() {
        try {
            embeddingUseCase.removeAllDocuments();
            log.warn("Todos los embeddings han sido eliminados vía endpoint.");
            return ResponseEntity.ok("Todos los embeddings fueron eliminados correctamente.");
        } catch (Exception e) {
            log.error("Error eliminando embeddings", e);
            return ResponseEntity.status(500).body("Error eliminando embeddings: " + e.getMessage());
        }
    }


}