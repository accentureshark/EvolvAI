package org.shark.evolvai.embedding.adapter.input.rest;

import org.shark.evolvai.embedding.port.input.EmbeddingUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingController {

    private final EmbeddingUseCase embeddingUseCase;

    public EmbeddingController(EmbeddingUseCase embeddingUseCase) {
        this.embeddingUseCase = embeddingUseCase;
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
        return ResponseEntity.ok(embeddingUseCase.findSimilarDocuments(query, maxResults, minScore));
    }
}

