package org.shark.evolvai.embedding.adapter.in.rest;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.shark.evolvai.config.RagProperties;
import org.shark.evolvai.embedding.domain.service.SmartChunkingService;
import org.shark.evolvai.embedding.port.in.EmbeddingUseCase;
import org.shark.evolvai.metrics.MonitoredEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/embeddings")
@Tag(
        name = "Embeddings API",
        description = """
                API para gestionar embeddings de documentos, incluyendo indexación,
                búsqueda y administración de vectores
                """
)
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
        this.smartChunkingService = smartChunkingService;

        this.ragProperties = ragProperties;
    }


    @Operation(
            summary = "Sube un documento para indexar",
            description = """
                    Permite subir archivos .txt, .pdf o .json estructurados para ser indexados
                    y embebidos en la base de datos de embeddings.
                    """,
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Documento procesado correctamente"
                    ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Solicitud inválida o tipo de archivo no soportado"
                    ),
                @ApiResponse(
                        responseCode = "500",
                        description = "Error procesando el documento"
                    )
            }
    )
    @PostMapping("/upload")
    @MonitoredEndpoint(name = "api.embedding.upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            if (filename == null) {
                log.warn("Archivo inválido: nombre nulo");
                return ResponseEntity.badRequest().build();
            }

            Map<String, String> baseMetadata = new HashMap<>();
            List<TextSegment> chunks = List.of();
            Object input = null;

            // Forzar siempre documentId y originalFile al nombre del archivo subido
            baseMetadata.put("documentId", filename);
            baseMetadata.put("originalFile", filename);

            // JSON estructurado
            if (filename.endsWith(".json")) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> doc = mapper.readValue(
                        file.getInputStream(),
                        new TypeReference<>() {}
                );
                Object metadataObj = doc.get("metadata");
                if (metadataObj instanceof Map) {
                    baseMetadata.putAll((Map<String, String>) metadataObj);
                }
                input = doc.get("data");
            } else {
                DocumentParser parser = filename.endsWith(".pdf")
                        ? new ApachePdfBoxDocumentParser()
                        : new ApacheTikaDocumentParser();
                input = parser.parse(file.getInputStream());
            }

            chunks = smartChunkingService.chunk(input, baseMetadata);

            if (chunks.isEmpty()) {
                log.warn("No se generaron chunks para el documento {}", filename);
                return ResponseEntity.badRequest().body(
                        "No se generaron fragmentos útiles del documento."
                );
            }

            embeddingUseCase.index(chunks);
            log.info(
                    "Documento {} indexado correctamente con {} fragmentos.",
                    filename, chunks.size()
            );

            return ResponseEntity.ok(
                    "Documento procesado correctamente con "
                            + chunks.size()
                            + " fragmentos."
            );
        } catch (Exception e) {
            log.error("Error procesando el documento", e);
            return ResponseEntity.status(500).body("Error procesando el documento.");
        }
    }

    @Operation(
            summary = "Indexa un documento como embeddings",
            description = """
                    Permite enviar un documento en texto plano para que sea fragmentado, 
                    convertido en embeddings y almacenado. Puede incluir un prompt personalizado
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Documento a indexar con texto plano y metadatos opcionales"
            ),
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Documento indexado correctamente"
                    ),
                @ApiResponse(
                        responseCode = "500",
                        description = "Error interno"
                    )
            }
    )
    @PostMapping("/index")
    @MonitoredEndpoint(name = "api.embedding.index")
    public ResponseEntity<Void> indexDocument(@RequestBody DocumentRequest request) {
        String id = request.getId() != null ? request.getId() : UUID.randomUUID().toString();
        embeddingUseCase.indexDocument(id, request.getText(), request.getCustomPrompt());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Buscar documentos similares por embedding",
            description = """
                    Genera un embedding a partir de la consulta y busca documentos previamente 
                    indexados que tengan una similitud vectorial alta
                    """,
            parameters = {
                @Parameter(
                        name = "query",
                        required = true,
                        description = "Texto base para generar el embedding y comparar"
                    ),
                @Parameter(
                        name = "maxResults",
                        required = false,
                        description = "Máximo de resultados a devolver (opcional)"
                    ),
                @Parameter(
                        name = "minScore",
                        required = false,
                        description = "Puntaje mínimo de similitud (opcional)"
                    )
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Lista de document IDs similares"
                    ),
                @ApiResponse(
                        responseCode = "500",
                        description = "Error interno al procesar la búsqueda"
                    )
            }
    )
    @GetMapping("/search")
    @MonitoredEndpoint(name = "api.embedding.search")
    public ResponseEntity<List<String>> search(
            @RequestParam String query,
            @RequestParam(required = false) Integer maxResults,
            @RequestParam(required = false) Double minScore) {
        int results = maxResults != null
                ? maxResults
                : ragProperties.getInference().getMaxResults();
        double score = minScore != null
                ? minScore
                : ragProperties.getInference().getMinScore();
        List<String> resultsList = embeddingUseCase.findSimilarDocuments(query, results, score);
        return ResponseEntity.ok(resultsList);
    }

    @Operation(
            summary = "Listar todos los document_id indexados",
            description = """
                Devuelve una lista de identificadores únicos de documentos que han sido 
                previamente embebidos y almacenados en el sistema. 
                Útil para auditar o recuperar metadatos
                """,
            responses = {
                @ApiResponse(
                    responseCode = "200",
                    description = "Lista de document_id únicos"
                    ),
                @ApiResponse(
                    responseCode = "500",
                    description = "Error al consultar los document_id")
            }
    )
    @GetMapping("/documents")
    @MonitoredEndpoint(name = "api.embedding.documents")
    public ResponseEntity<List<String>> listDocuments() {
        log.info("Recibida solicitud para listar document_id distintos.");
        List<String> ids = embeddingUseCase.listDocumentIds();
        log.info("Se encontraron {} document_id distintos.", ids.size());
        return ResponseEntity.ok(ids);
    }

    @Operation(
            summary = "Elimina todos los embeddings del almacenamiento",
            description = "Borra todos los embeddings actualmente almacenados en la BD",
            responses = {
                @ApiResponse(
                    responseCode = "200",
                    description = "Todos los embeddings fueron eliminados correctamente"
                    ),
                @ApiResponse(
                    responseCode = "500",
                    description = "Error eliminando embeddings"
                    )
            }
    )
    @DeleteMapping("/remove-all")
    @MonitoredEndpoint(name = "api.embedding.remove-all")
    public ResponseEntity<String> removeAllEmbeddings() {
        try {
            embeddingUseCase.removeAllDocuments();
            log.warn("Todos los embeddings han sido eliminados vía endpoint.");
            return ResponseEntity.ok("Todos los embeddings fueron eliminados correctamente.");
        } catch (Exception e) {
            log.error("Error eliminando embeddings", e);
            return ResponseEntity.status(500).body(
                "Error eliminando embeddings: " + e.getMessage()
            );
        }
    }
}