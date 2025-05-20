package org.shark.evolvai.embedding.adapter.input.rest;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import org.shark.evolvai.embedding.port.input.EmbeddingUseCase;
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

    public EmbeddingController(EmbeddingUseCase embeddingUseCase, HuggingFaceTokenizer tokenizer) {
        this.embeddingUseCase = embeddingUseCase;
        this.tokenizer = tokenizer;
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

            var embedding = embeddingUseCase.generateEmbedding(inputIds, attentionMask); // <--- asegurate que esto esté acá
            log.info("Archivo procesado exitosamente: {}", file.getOriginalFilename());

            return ResponseEntity.ok(embedding.vectorAsList()); // <--- y esto también dentro del try

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
        embeddingUseCase.indexDocument(id, request.getText());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<String>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int maxResults,
            @RequestParam(defaultValue = "0.7") double minScore) {
        List<String> results = embeddingUseCase.findSimilarDocuments(query, maxResults, minScore);
        return ResponseEntity.ok(results);
    }
}
