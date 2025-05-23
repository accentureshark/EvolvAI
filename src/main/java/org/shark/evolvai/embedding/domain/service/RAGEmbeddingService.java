package org.shark.evolvai.embedding.domain.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.shark.evolvai.embedding.port.input.EmbeddingUseCase;
import org.shark.evolvai.embedding.port.output.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Primary
public class RAGEmbeddingService implements EmbeddingUseCase {

    private static final Logger log = LoggerFactory.getLogger(RAGEmbeddingService.class);
    private static final String DEFAULT_PROMPT = "Genera embeddings para este texto:";

    private final EmbeddingGenerator embeddingGenerator;
    private final EmbeddingStorage embeddingStorage;
    private final TextChunkingService textChunkingService;

    public RAGEmbeddingService(
            EmbeddingGenerator embeddingGenerator,
            EmbeddingStorage embeddingStorage,
            TextChunkingService textChunkingService) {
        this.embeddingGenerator = embeddingGenerator;
        this.embeddingStorage = embeddingStorage;
        this.textChunkingService = textChunkingService;
    }

    @Override
    public void indexDocument(String id, String text, String customPrompt) {
        log.info("Indexando documento con id: {}", id);
        String prompt = (customPrompt != null && !customPrompt.isBlank()) ? customPrompt : DEFAULT_PROMPT;

        List<TextSegment> segments = textChunkingService.chunk(text, id);

        for (TextSegment segment : segments) {
            String promptFragment = prompt + "\n" + segment.text();
            Embedding embedding = embeddingGenerator.generateEmbedding(new TextSegment(promptFragment, segment.metadata()));

            // Definir un documentName robusto y seguro
            String docName = id;
            if (segment.metadata() != null && segment.metadata().asMap().containsKey("chunkIndex")) {
                docName += "/fragment-" + segment.metadata().asMap().get("chunkIndex");
            }

            embeddingStorage.store(docName, embedding, segment.text(), segment.metadata());
            log.info("Fragmento almacenado con id: {}", docName);
        }

        log.info("Documento indexado correctamente: {}", id);
    }

    @Override
    public List<String> listDocumentIds() {
        return embeddingStorage.findAllDocumentIds();
    }

    @Override
    public List<String> findSimilarDocuments(String query, int maxResults, double minScore) {
        log.info("Buscando documentos similares para query: '{}', maxResults: {}, minScore: {}", query, maxResults, minScore);
        Embedding queryEmbedding = embeddingGenerator.generateEmbedding(query);
        List<EmbeddingMatch<String>> matches = embeddingStorage.findSimilar(queryEmbedding, maxResults, minScore);
        log.info("Se encontraron {} documentos similares.", matches.size());
        return matches.stream()
                .map(EmbeddingMatch::embedded)
                .collect(Collectors.toList());
    }

    @Override
    public Embedding generateEmbedding(String text) {
        log.debug("Generando embedding para texto.");
        return embeddingGenerator.generateEmbedding(text);
    }

    @Override
    public Embedding generateEmbedding(int[] inputIds, int[] attentionMask) {
        log.debug("Generando embedding para inputIds y attentionMask.");
        return embeddingGenerator.generateEmbedding(inputIds, attentionMask);
    }

    @Override
    public void removeAllDocuments() {
        log.warn("Eliminando todos los documentos indexados.");
        embeddingStorage.removeAll();
    }
}
