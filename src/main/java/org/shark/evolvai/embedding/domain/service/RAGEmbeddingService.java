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
    public void indexDocument(String id, String text) {
        log.info("Indexando documento con id: {}", id);


        List<String> fragments = textChunkingService.chunk(text)
                .stream()
                .map(TextSegment::text)
                .collect(Collectors.toList());


        for (int i = 0; i < fragments.size(); i++) {
            String fragment = fragments.get(i);
            Embedding embedding = embeddingGenerator.generateEmbedding(fragment);
            String fragmentId = id + "-fragment-" + i;
            embeddingStorage.store(fragmentId, embedding, fragment);
            log.info("Fragmento almacenado con id: {}", fragmentId);
        }

        log.info("Documento indexado correctamente: {}", id);
    }

    @Override
    public List<String> findSimilarDocuments(String query, int maxResults, double minScore) {
        log.info("Buscando documentos similares para query: '{}', maxResults: {}, minScore: {}", query, maxResults, minScore);
        Embedding queryEmbedding = embeddingGenerator.generateEmbedding(query);
        List<EmbeddingMatch<String>> matches = embeddingStorage.findSimilar(
                queryEmbedding, maxResults, minScore
        );
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