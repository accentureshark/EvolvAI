package org.shark.evolvai.embedding.adapter.in.rest;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import org.shark.evolvai.embedding.port.input.EmbeddingUseCase;
import org.shark.evolvai.inference.config.InferenceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingController {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingController.class);
    private final EmbeddingUseCase embeddingUseCase;
    private final HuggingFaceTokenizer tokenizer;
    private final InferenceProperties inferenceProperties;

    public EmbeddingController(
            EmbeddingUseCase embeddingUseCase,
            HuggingFaceTokenizer tokenizer,
            InferenceProperties inferenceProperties
    ) {
        this.embeddingUseCase = embeddingUseCase;
        this.tokenizer = tokenizer;
        this.inferenceProperties = inferenceProperties;
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
        int results = maxResults != null ? maxResults : inferenceProperties.getMaxResults();
        double score = minScore != null ? minScore : inferenceProperties.getMinScore();
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