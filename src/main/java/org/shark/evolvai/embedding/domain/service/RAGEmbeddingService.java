package org.shark.evolvai.embedding.domain.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.shark.evolvai.embedding.port.input.EmbeddingUseCase;
import org.shark.evolvai.embedding.port.output.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RAGEmbeddingService implements EmbeddingUseCase {

    private final EmbeddingGenerator embeddingGenerator;
    private final EmbeddingStorage embeddingStorage;

    public RAGEmbeddingService(
            EmbeddingGenerator embeddingGenerator,
            EmbeddingStorage embeddingStorage) {
        this.embeddingGenerator = embeddingGenerator;
        this.embeddingStorage = embeddingStorage;
    }

    @Override
    public void indexDocument(String id, String text) {
        Embedding embedding = embeddingGenerator.generateEmbedding(text);
        embeddingStorage.store(id, embedding, text);
    }

    @Override
    public List<String> findSimilarDocuments(String query, int maxResults, double minScore) {
        Embedding queryEmbedding = embeddingGenerator.generateEmbedding(query);
        List<EmbeddingMatch<String>> matches = embeddingStorage.findSimilar(
                queryEmbedding, maxResults, minScore);

        return matches.stream()
                .map(EmbeddingMatch::embedded)
                .collect(Collectors.toList());
    }

    @Override
    public Embedding generateEmbedding(String text) {
        return embeddingGenerator.generateEmbedding(text);
    }

    @Override
    public Embedding generateEmbedding(int[] inputIds, int[] attentionMask) {
        return embeddingGenerator.generateEmbedding(inputIds, attentionMask);
    }

    @Override
    public void removeAllDocuments() {
        embeddingStorage.removeAll();
    }
}
